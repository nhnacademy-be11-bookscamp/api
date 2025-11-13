package store.bookscamp.api.orderinfo.service.dto;

public record OrderItemRequestDto(
        Long bookId,
        Integer quantity
) {
}