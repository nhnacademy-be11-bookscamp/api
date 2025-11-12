package store.bookscamp.api.orderinfo.service.dto;

import java.time.LocalDate;

public record DeliveryInfoDto(
        String recipientName,
        String recipientPhone,
        Integer zipCode,
        String roadNameAddress,
        String detailAddress,
        LocalDate desiredDeliveryDate,
        String deliveryMemo
) {
}