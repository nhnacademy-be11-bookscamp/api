package store.bookscamp.api.orderinfo.service.dto;

import store.bookscamp.api.orderinfo.entity.OrderStatus;

public record OrderStatusUpdateDto(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus
) {
}