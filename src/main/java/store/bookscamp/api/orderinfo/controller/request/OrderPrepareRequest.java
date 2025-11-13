package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareRequestDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemRequestDto;

public record OrderPrepareRequest(
        @NotEmpty(message = "주문 도서는 최소 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> items
) {
    public OrderPrepareRequestDto toDto() {
        return new OrderPrepareRequestDto(
                items.stream()
                        .map(item -> new OrderItemRequestDto(item.bookId(), item.quantity()))
                        .toList()
        );
    }
}