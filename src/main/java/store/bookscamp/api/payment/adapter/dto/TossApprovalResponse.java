package store.bookscamp.api.payment.adapter.dto;

public record TossApprovalResponse(
        String paymentKey,
        String orderId,
        int totalAmount,
        String method,
        String approvedAt
) {
}