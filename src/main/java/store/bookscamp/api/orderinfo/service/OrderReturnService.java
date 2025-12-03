package store.bookscamp.api.orderinfo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.ReturnType;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderReturnDto;
import store.bookscamp.api.orderinfo.service.dto.OrderReturnRequestDto;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static store.bookscamp.api.common.exception.ErrorCode.*;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.RETURNED;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderReturnService {

    private final OrderInfoRepository orderInfoRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final OrderStatusService orderStatusService;
    private final DeliveryPolicyRepository deliveryPolicyRepository;

    public OrderReturnDto returnOrder(OrderReturnRequestDto dto, Long orderId) {
        OrderInfo order = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ORDER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        long period = ChronoUnit.DAYS.between(order.getDelivery().getShippingDate(), now);
        int allowableDays = dto.returnType().getAllowableDays();

        if (period > allowableDays) {
            throw new ApplicationException(ORDER_RETURN_PERIOD_EXPIRED);
        }

        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ApplicationException(DELIVERY_POLICY_NOT_CONFIGURED));
        int deliveryFee = deliveryPolicy.getBaseDeliveryFee();

        int refundAmount = order.getFinalPaymentAmount();

        if (dto.returnType() == ReturnType.CHANGE_OF_MIND) {
            refundAmount -= deliveryFee;
        }

        List<PointHistory> histories = pointHistoryRepository.findByOrderInfo_Id(orderId);

        int usedPoint = histories.stream()
                .filter(h -> h.getPointAmount() < 0)
                .mapToInt(PointHistory::getPointAmount)
                .sum();

        int earnedPoint = histories.stream()
                .filter(h -> h.getPointAmount() > 0)
                .mapToInt(PointHistory::getPointAmount)
                .sum();

        Member member = order.getMember();
        if (member != null) {
            if (earnedPoint > 0) {
                member.usePoint(earnedPoint);
                PointHistory deductHistory = PointHistory.use(
                        order,
                        member,
                        earnedPoint,
                        "반품 - 구매 시 적립 포인트 회수"
                );
                pointHistoryRepository.save(deductHistory);
            }

            int refundPoints = refundAmount + Math.abs(usedPoint);
            if (refundPoints > 0) {
                member.earnPoint(refundPoints);
                PointHistory refundHistory = PointHistory.earn(
                        order,
                        member,
                        refundPoints,
                        "반품 - 환불 포인트 적립"
                );
                pointHistoryRepository.save(refundHistory);
            }

            CouponIssue couponIssue = order.getCouponIssue();
            if (couponIssue != null) {
                couponIssue.restore();
            }
        }

        orderStatusService.updateOrderStatus(orderId, RETURNED);

        int finalRefundPoints = refundAmount + Math.abs(usedPoint) - earnedPoint;

        OrderReturnDto result = new OrderReturnDto(order.getOrderNumber(), finalRefundPoints);

        log.info("[ORDER-RETURN] 반품 처리 완료 - orderNumber: {}, returnType: {}, finalRefundPoints: {}, " +
                "refundAmount: {}, usedPoint: {}, earnedPoint: {}",
                order.getOrderNumber(), dto.returnType(), finalRefundPoints,
                refundAmount, Math.abs(usedPoint), earnedPoint);

        return result;
    }
}
