package store.bookscamp.api.payment.adapter.dto;

public record TossApprovalRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}