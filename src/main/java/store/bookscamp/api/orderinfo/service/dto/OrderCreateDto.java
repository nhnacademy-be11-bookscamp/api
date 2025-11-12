package store.bookscamp.api.orderinfo.service.dto;

public record OrderCreateDto(
        Long orderId,
        Integer finalPaymentAmount
) {
}