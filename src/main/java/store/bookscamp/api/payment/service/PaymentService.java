package store.bookscamp.api.payment.service;

import static store.bookscamp.api.common.exception.ErrorCode.*;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.payment.adapter.PaymentAdapter;
import store.bookscamp.api.payment.adapter.PaymentApprovalResponse;
import store.bookscamp.api.payment.entity.Payment;
import store.bookscamp.api.payment.entity.PaymentMethod;
import store.bookscamp.api.payment.entity.PaymentProvider;
import store.bookscamp.api.payment.repository.PaymentRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentAdapter paymentAdapter;

    public Payment confirmPayment(String paymentKey, String orderNumber, int amount) {
        log.info("[PAYMENT-CONFIRM-SERVICE] START - orderNumber={}, paymentKey={}, amount={}", orderNumber, paymentKey, amount);

        // 1. 주문 조회 및 검증
        OrderInfo orderInfo = orderInfoRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ApplicationException(ORDER_NOT_FOUND));
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP1 - Order found: orderId={}, status={}", orderInfo.getId(), orderInfo.getOrderStatus());

        // 2. 주문 상태 검증 (결제 대기 중이어야 함)
        if (orderInfo.getOrderStatus() != AWAITING_PAYMENT) {
            log.error("[PAYMENT-CONFIRM-SERVICE] ERROR - Invalid order status: {}", orderInfo.getOrderStatus());
            throw new ApplicationException(ORDER_NOT_AWAITING_PAYMENT);
        }

        // 3. 이미 결제된 주문인지 확인
        if (paymentRepository.existsByOrderInfo(orderInfo)) {
            log.error("[PAYMENT-CONFIRM-SERVICE] ERROR - Order already paid: orderId={}", orderInfo.getId());
            throw new ApplicationException(ORDER_ALREADY_PAID);
        }

        // 4. 결제 금액 검증
        if (orderInfo.getFinalPaymentAmount() != amount) {
            log.error("[PAYMENT-CONFIRM-SERVICE] ERROR - Amount mismatch: expected={}, actual={}", orderInfo.getFinalPaymentAmount(), amount);
            throw new ApplicationException(PAYMENT_AMOUNT_MISMATCH);
        }
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP2 - Validation passed");

        // 5. 토스 결제 승인 요청
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP3 - Requesting Toss approval...");
        PaymentApprovalResponse approvalResponse = paymentAdapter.approve(
                paymentKey,
                orderNumber,
                amount
        );
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP4 - Toss approval success: paymentKey={}", approvalResponse.paymentKey());

        // 6. Payment 저장
        Payment payment = Payment.builder()
                .orderInfo(orderInfo)
                .paidAmount(approvalResponse.totalAmount())
                .paidAt(approvalResponse.approvedAt())
                .paymentKey(approvalResponse.paymentKey())
                .paymentMethod(PaymentMethod.CARD)
                .paymentProvider(PaymentProvider.TOSS)
                .build();
        paymentRepository.save(payment);
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP5 - Payment saved: paymentId={}", payment.getId());

        // 7. 재고 차감
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP6 - Processing stock decrease...");
        processStockDecrease(orderInfo);
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP7 - Stock decrease completed");

        // 8. 포인트/쿠폰 처리 (회원만)
        if (orderInfo.getMember() != null) {
            log.info("[PAYMENT-CONFIRM-SERVICE] STEP8 - Processing member benefits for memberId={}...", orderInfo.getMember().getId());
            processMemberBenefits(orderInfo);
            log.info("[PAYMENT-CONFIRM-SERVICE] STEP9 - Member benefits processed");
        }

        // 9. 주문 상태 변경 (AWAITING_PAYMENT → PENDING)
        orderInfo.changeOrderStatus(PENDING);
        log.info("[PAYMENT-CONFIRM-SERVICE] STEP10 - Order status changed to PENDING");

        log.info("[PAYMENT-CONFIRM-SERVICE] SUCCESS - paymentId={}, orderId={}", payment.getId(), orderInfo.getId());
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

        // 포인트 사용 처리
        if (usedPoint > 0) {
            member.usePoint(usedPoint);
            PointHistory useHistory = new PointHistory(
                    orderInfo,
                    member,
                    PointType.USE,
                    usedPoint,
                    "주문 사용"
            );
            pointHistoryRepository.save(useHistory);
        }

        // 쿠폰 사용 처리
        if (couponIssue != null) {
            couponIssue.use();
        }

        // 포인트 적립
        int earnedPoint = calculateEarnedPoint(member, orderInfo.getNetAmount());
        if (earnedPoint > 0) {
            member.earnPoint(earnedPoint);
            PointHistory earnHistory = new PointHistory(
                    orderInfo,
                    member,
                    PointType.EARN,
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
        // 1. 주문 조회
        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ORDER_NOT_FOUND));

        // 2. 취소 가능한 상태인지 확인 (배송 전: PENDING 상태만 가능)
        if (orderInfo.getOrderStatus() != PENDING) {
            throw new ApplicationException(ORDER_CANNOT_BE_CANCELLED);
        }

        // 3. 결제 정보 조회
        Payment payment = paymentRepository.findByOrderInfo(orderInfo)
                .orElseThrow(() -> new ApplicationException(PAYMENT_NOT_FOUND));

        // 4. 토스 결제 취소 요청
        paymentAdapter.cancel(payment.getPaymentKey(), cancelReason);

        // 5. 재고 복구
        rollbackStock(orderInfo);

        // 6. 포인트/쿠폰 복구 (회원만)
        if (orderInfo.getMember() != null) {
            rollbackMemberBenefits(orderInfo);
        }

        // 7. 주문 상태 변경 (PENDING → CANCELLED)
        orderInfo.changeOrderStatus(CANCELLED);

        // 8. Payment soft delete
        paymentRepository.delete(payment);
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

        // 사용한 포인트 복구
        if (usedPoint > 0) {
            member.earnPoint(usedPoint);
            PointHistory refundHistory = new PointHistory(
                    orderInfo,
                    member,
                    PointType.EARN,
                    usedPoint,
                    "결제 취소 - 사용 포인트 복구"
            );
            pointHistoryRepository.save(refundHistory);
        }

        // 적립된 포인트 차감 (적립 포인트 롤백)
        int earnedPoint = calculateEarnedPoint(member, orderInfo.getNetAmount());
        if (earnedPoint > 0) {
            member.usePoint(earnedPoint);
            PointHistory cancelEarnHistory = new PointHistory(
                    orderInfo,
                    member,
                    PointType.USE,
                    earnedPoint,
                    "결제 취소 - 적립 포인트 차감"
            );
            pointHistoryRepository.save(cancelEarnHistory);
        }

        // 쿠폰 복구 (사용 전 상태로)
        if (couponIssue != null) {
            couponIssue.restore();
        }
    }
}