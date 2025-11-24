package store.bookscamp.api.payment.adapter;

import java.time.LocalDateTime;

public record PaymentApprovalResponse(
        String paymentKey,
        String orderId,
        int totalAmount,
        String method,
        LocalDateTime approvedAt
) {
}