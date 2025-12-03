package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.service.dto.OrderReturnDto;

public record OrderReturnResponse(
        String OrderNumber,
        OrderStatus orderStatus,
        int point
) {
    public static OrderReturnResponse fromDto(OrderReturnDto dto) {
        return new OrderReturnResponse(dto.orderNumber(), dto.orderStatus(), dto.point());
    }
}
