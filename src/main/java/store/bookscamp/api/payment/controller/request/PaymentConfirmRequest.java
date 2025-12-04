package store.bookscamp.api.payment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentConfirmRequest(
        String paymentKey,

        @NotBlank(message = "주문 번호는 필수입니다.")
        String orderNumber,

        @NotNull(message = "결제 금액은 필수입니다.")
        @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
        Integer amount
) {
}