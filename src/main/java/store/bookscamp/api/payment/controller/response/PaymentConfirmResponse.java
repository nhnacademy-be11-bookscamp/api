package store.bookscamp.api.payment.controller.response;

import java.time.LocalDateTime;

public record PaymentConfirmResponse(
        Long paymentId,
        Long orderId,
        int paidAmount,
        LocalDateTime paidAt
) {
}