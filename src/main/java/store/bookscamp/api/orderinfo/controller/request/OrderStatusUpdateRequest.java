package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.orderinfo.entity.OrderStatus;

public record OrderStatusUpdateRequest(
        @NotNull(message = "주문 상태는 필수입니다")
        OrderStatus orderStatus
) {
}