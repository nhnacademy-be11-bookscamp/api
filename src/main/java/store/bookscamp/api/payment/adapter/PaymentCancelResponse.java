package store.bookscamp.api.payment.adapter;

import java.time.LocalDateTime;

public record PaymentCancelResponse(
        String paymentKey,
        String orderId,
        String cancelReason,
        LocalDateTime canceledAt
) {
}