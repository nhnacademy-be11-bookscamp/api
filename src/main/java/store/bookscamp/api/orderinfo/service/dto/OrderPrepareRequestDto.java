package store.bookscamp.api.orderinfo.service.dto;

import java.util.List;

public record OrderPrepareRequestDto(
        List<OrderItemRequestDto> items
) {
}