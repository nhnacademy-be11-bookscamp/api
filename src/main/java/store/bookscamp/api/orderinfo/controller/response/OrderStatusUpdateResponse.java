package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.service.dto.OrderStatusUpdateDto;

import java.time.LocalDateTime;

public record OrderStatusUpdateResponse(
        Long orderId,
        String orderNumber,
        OrderStatus currentStatus,
        LocalDateTime updatedAt
) {
    public static OrderStatusUpdateResponse fromDto(OrderStatusUpdateDto dto) {
        return new OrderStatusUpdateResponse(
                dto.orderId(),
                dto.orderNumber(),
                dto.orderStatus(),
                LocalDateTime.now()
        );
    }
}