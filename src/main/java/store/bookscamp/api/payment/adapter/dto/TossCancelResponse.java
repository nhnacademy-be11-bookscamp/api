package store.bookscamp.api.payment.adapter.dto;

import java.util.List;

public record TossCancelResponse(
        String paymentKey,
        String orderId,
        List<Cancel> cancels
) {
    public record Cancel(
            String canceledAt
    ) {}
}