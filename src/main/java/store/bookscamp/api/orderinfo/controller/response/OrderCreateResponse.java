package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;

public record OrderCreateResponse(
        Long orderId,
        Integer finalAmount
) {
    public static OrderCreateResponse fromDto(OrderCreateDto dto) {
        return new OrderCreateResponse(
                dto.orderId(),
                dto.finalPaymentAmount()
        );
    }
}