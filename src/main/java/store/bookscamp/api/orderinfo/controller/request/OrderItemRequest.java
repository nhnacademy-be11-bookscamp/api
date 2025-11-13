package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "도서 ID는 필수입니다.")
        Long bookId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}