package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.PriceDto;

public record PriceInfo(
        Integer netAmount,
        Integer deliveryFee,
        Integer totalAmount,
        Integer freeDeliveryThreshold
) {
    public static PriceInfo fromDto(PriceDto dto) {
        return new PriceInfo(
                dto.netAmount(),
                dto.deliveryFee(),
                dto.totalAmount(),
                dto.freeDeliveryThreshold()
        );
    }
}