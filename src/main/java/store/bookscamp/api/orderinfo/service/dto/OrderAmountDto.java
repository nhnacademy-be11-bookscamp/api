package store.bookscamp.api.orderinfo.service.dto;

public record OrderAmountDto(
        Integer netAmount,
        Integer packagingFee,
        Integer deliveryFee,
        Integer totalAmount
) {
}
