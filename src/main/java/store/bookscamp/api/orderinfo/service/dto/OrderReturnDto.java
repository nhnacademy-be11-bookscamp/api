package store.bookscamp.api.orderinfo.service.dto;

import store.bookscamp.api.orderinfo.entity.OrderStatus;

public record OrderReturnDto(
        String orderNumber,
        OrderStatus orderStatus,
        int point
) {
}
