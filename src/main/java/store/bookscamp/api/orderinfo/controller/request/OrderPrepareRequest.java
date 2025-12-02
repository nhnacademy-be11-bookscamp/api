package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import store.bookscamp.api.orderinfo.entity.OrderType;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareRequestDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemRequestDto;

public record OrderPrepareRequest(
        @NotEmpty(message = "주문 도서는 최소 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "주문 타입은 필수입니다.")
        OrderType orderType
) {
    public OrderPrepareRequestDto toDto() {
        return new OrderPrepareRequestDto(
                items.stream()
                        .map(item -> new OrderItemRequestDto(item.bookId(), item.quantity()))
                        .toList()
        );
    }
}