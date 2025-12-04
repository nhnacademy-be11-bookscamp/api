package store.bookscamp.api.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.cart.service.CartService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.OrderCartMappingService;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.payment.adapter.PaymentAdapter;
import store.bookscamp.api.payment.adapter.PaymentApprovalResponse;
import store.bookscamp.api.payment.entity.Payment;
import store.bookscamp.api.payment.entity.PaymentMethod;
import store.bookscamp.api.payment.entity.PaymentProvider;
import store.bookscamp.api.payment.repository.PaymentRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;

import java.util.List;

import static store.bookscamp.api.common.exception.ErrorCode.ORDER_ALREADY_PAID;
import static store.bookscamp.api.common.exception.ErrorCode.ORDER_CANNOT_BE_CANCELLED;
import static store.bookscamp.api.common.exception.ErrorCode.ORDER_NOT_AWAITING_PAYMENT;
import static store.bookscamp.api.common.exception.ErrorCode.ORDER_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.PAYMENT_AMOUNT_MISMATCH;
import static store.bookscamp.api.common.exception.ErrorCode.PAYMENT_NOT_FOUND;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.AWAITING_PAYMENT;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.CANCELLED;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.PENDING;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentAdapter paymentAdapter;
    private final OrderCartMappingService orderCartMappingService;
    private final CartService cartService;

    public Payment confirmPayment(String paymentKey, String orderNumber, int amount) {
        OrderInfo orderInfo = orderInfoRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ApplicationException(ORDER_NOT_FOUND));

        if (orderInfo.getOrderStatus() != AWAITING_PAYMENT) {
            throw new ApplicationException(ORDER_NOT_AWAITING_PAYMENT);
        }

        if (paymentRepository.existsByOrderInfo(orderInfo)) {
            throw new ApplicationException(ORDER_ALREADY_PAID);
        }

        if (orderInfo.getFinalPaymentAmount() != amount) {
            throw new ApplicationException(PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment;

        if (amount == 0) {
            payment = new Payment(
                    orderInfo,
                    0,
                    null,
                    null,
                    PaymentMethod.POINT,
                    PaymentProvider.INTERNAL
            );
        } else {
            PaymentApprovalResponse approvalResponse = paymentAdapter.approve(
                    paymentKey,
                    orderNumber,
                    amount
            );

            payment = new Payment(
                    orderInfo,
                    approvalResponse.totalAmount(),
                    approvalResponse.approvedAt(),
                    approvalResponse.paymentKey(),
                    PaymentMethod.CARD,
                    PaymentProvider.TOSS
            );
        }

        paymentRepository.save(payment);

        processStockDecrease(orderInfo);

        if (orderInfo.getMember() != null) {
            processMemberBenefits(orderInfo);
        }

        Long cartId = orderCartMappingService.getAndDeleteMapping(orderNumber);
        if (cartId != null) {
            cartService.clearCart(cartId);
            log.info("[PAYMENT] 장바구니 비우기 완료 - cartId: {}", cartId);
        }

        orderInfo.changeOrderStatus(PENDING);
        return payment;
    }

    private void processStockDecrease(OrderInfo orderInfo) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderInfo.getId());

        for (OrderItem orderItem : orderItems) {
            Book book = orderItem.getBook();
            book.decreaseStock(orderItem.getOrderQuantity());
        }
    }

    private void processMemberBenefits(OrderInfo orderInfo) {
        Member member = orderInfo.getMember();
        int usedPoint = orderInfo.getUsedPoint();
        CouponIssue couponIssue = orderInfo.getCouponIssue();

        if (usedPoint > 0) {
            member.usePoint(usedPoint);
            PointHistory useHistory = PointHistory.use(
                    orderInfo,
                    member,
                    usedPoint,
                    "주문 사용"
            );
            pointHistoryRepository.save(useHistory);
        }

        if (couponIssue != null) {
            couponIssue.use();
        }

        int earnedPoint = calculateEarnedPoint(member, orderInfo.getNetAmount());
        if (earnedPoint > 0) {
            member.earnPoint(earnedPoint);
            PointHistory earnHistory = PointHistory.earn(
                    orderInfo,
                    member,
                    earnedPoint,
                    "주문 적립"
            );
            pointHistoryRepository.save(earnHistory);
        }
    }

    private int calculateEarnedPoint(Member member, int netAmount) {
        if (member.getRank() == null) {
            return 0;
        }

        PointPolicy pointPolicy = member.getRank().getPointPolicy();
        if (pointPolicy == null) {
            return 0;
        }

        return switch (pointPolicy.getRewardType()) {
            case RATE -> (int) Math.floor(netAmount * pointPolicy.getRewardValue() / 100.0);
            case AMOUNT -> pointPolicy.getRewardValue();
        };
    }

    public void cancelPayment(Long orderId, String cancelReason) {
        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ORDER_NOT_FOUND));

        if (orderInfo.getOrderStatus() != PENDING) {
            throw new ApplicationException(ORDER_CANNOT_BE_CANCELLED);
        }

        Payment payment = paymentRepository.findByOrderInfo(orderInfo)
                .orElseThrow(() -> new ApplicationException(PAYMENT_NOT_FOUND));

        if (payment.getPaymentKey() != null) {
            paymentAdapter.cancel(payment.getPaymentKey(), cancelReason);
            log.info("[PAYMENT-CANCEL] Toss 취소 완료 - orderId: {}", orderId);
        } else {
            log.info("[PAYMENT-CANCEL] 포인트/쿠폰 결제 취소 - orderId: {}", orderId);
        }

        rollbackStock(orderInfo);
        log.info("[PAYMENT-CANCEL] 재고 복구 완료 - orderId: {}", orderId);

        if (orderInfo.getMember() != null) {
            rollbackMemberBenefits(orderInfo);
            log.info("[PAYMENT-CANCEL] 회원 혜택 복구 완료 - orderId: {}", orderId);
        }

        orderInfo.changeOrderStatus(CANCELLED);
        log.info("[PAYMENT-CANCEL] 주문 상태 변경 완료 - orderId: {}, status: CANCELLED", orderId);

        paymentRepository.delete(payment);
        log.info("[PAYMENT-CANCEL] 결제 정보 삭제 완료 - orderId: {}", orderId);
    }

    private void rollbackStock(OrderInfo orderInfo) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderInfo.getId());

        for (OrderItem orderItem : orderItems) {
            Book book = orderItem.getBook();
            book.increaseStock(orderItem.getOrderQuantity());
        }
    }

    private void rollbackMemberBenefits(OrderInfo orderInfo) {
        Member member = orderInfo.getMember();
        int usedPoint = orderInfo.getUsedPoint();
        CouponIssue couponIssue = orderInfo.getCouponIssue();

        if (usedPoint > 0) {
            member.earnPoint(usedPoint);
            PointHistory refundHistory = PointHistory.earn(
                    orderInfo,
                    member,
                    usedPoint,
                    "결제 취소 - 사용 포인트 복구"
            );
            pointHistoryRepository.save(refundHistory);
        }

        int earnedPoint = calculateEarnedPoint(member, orderInfo.getNetAmount());
        if (earnedPoint > 0) {
            member.usePoint(earnedPoint);
            PointHistory cancelEarnHistory = PointHistory.use(
                    orderInfo,
                    member,
                    earnedPoint,
                    "결제 취소 - 적립 포인트 차감"
            );
            pointHistoryRepository.save(cancelEarnHistory);
        }

        if (couponIssue != null) {
            couponIssue.restore();
        }
    }
}