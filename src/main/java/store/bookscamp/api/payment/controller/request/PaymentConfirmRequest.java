package store.bookscamp.api.payment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제 키는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "주문 번호는 필수입니다.")
        String orderNumber,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 양수여야 합니다.")
        Integer amount
) {
}