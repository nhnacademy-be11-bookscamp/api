package store.bookscamp.api.payment.adapter.dto;

public record TossCancelResponse(
        String paymentKey,
        String orderId,
        String canceledAt
) {
}