package store.bookscamp.api.orderinfo.service.dto;

public record PriceDto(
        Integer netAmount,
        Integer deliveryFee,
        Integer totalAmount,
        Integer freeDeliveryThreshold
) {
}