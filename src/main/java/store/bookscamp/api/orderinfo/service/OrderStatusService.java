package store.bookscamp.api.orderinfo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderStatusUpdateDto;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderStatusService {

    private final OrderInfoRepository orderInfoRepository;

    public OrderStatusUpdateDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("[ORDER-STATUS-UPDATE] 주문 상태 변경 시작 - orderId: {}, newStatus: {}", orderId, newStatus);

        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus previousStatus = orderInfo.getOrderStatus();

        validateStatusTransition(previousStatus, newStatus);

        orderInfo.changeOrderStatus(newStatus);

        log.info("[ORDER-STATUS-UPDATE] 주문 상태 변경 완료 - orderId: {}, {} → {}",
                orderId, previousStatus, newStatus);

        return new OrderStatusUpdateDto(
                orderInfo.getId(),
                orderInfo.getOrderNumber(),
                orderInfo.getOrderStatus()
        );
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == target) {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_ALREADY_SET);
        }

        if (current == OrderStatus.CANCELLED) {
            throw new ApplicationException(ErrorCode.CANCELLED_ORDER_CANNOT_BE_UPDATED);
        }

        if (current == OrderStatus.RETURNED) {
            throw new ApplicationException(ErrorCode.RETURNED_ORDER_CANNOT_BE_UPDATED);
        }

        if (current == OrderStatus.DELIVERED && target != OrderStatus.RETURNED) {
            throw new ApplicationException(ErrorCode.DELIVERED_ORDER_CAN_ONLY_BE_RETURNED);
        }

        if (current == OrderStatus.AWAITING_PAYMENT) {
            throw new ApplicationException(ErrorCode.AWAITING_PAYMENT_ORDER_CANNOT_BE_UPDATED);
        }

        if (current == OrderStatus.PENDING && target != OrderStatus.SHIPPING) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        if (current == OrderStatus.SHIPPING && target != OrderStatus.DELIVERED) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
    }

    public int autoCompleteShippingOrders() {
        log.info("[AUTO-COMPLETE] 배송 완료 자동 처리 시작");

        LocalDate today = LocalDate.now();

        List<OrderInfo> shippingOrders = orderInfoRepository.findShippingOrdersToComplete(
                OrderStatus.SHIPPING,
                today
        );

        int completedCount = 0;
        for (OrderInfo order : shippingOrders) {
            order.changeOrderStatus(OrderStatus.DELIVERED);
            completedCount++;
            log.info("[AUTO-COMPLETE] 주문 완료 처리 - orderId: {}, orderNumber: {}, shippingDate: {}",
                    order.getId(), order.getOrderNumber(), order.getDelivery().getShippingDate());
        }

        log.info("[AUTO-COMPLETE] 배송 완료 자동 처리 완료 - 처리 건수: {}", completedCount);
        return completedCount;
    }
}