package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.OrderReturnDto;

public record OrderReturnResponse(
        String orderNumber,
        int point
) {
    public static OrderReturnResponse fromDto(OrderReturnDto dto) {
        return new OrderReturnResponse(dto.orderNumber(), dto.point());
    }
}
