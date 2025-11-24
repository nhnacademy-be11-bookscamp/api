package store.bookscamp.api.payment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCancelRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        @Positive(message = "주문 ID는 양수여야 합니다.")
        Long orderId,

        @NotBlank(message = "취소 사유는 필수입니다.")
        String cancelReason
) {
}