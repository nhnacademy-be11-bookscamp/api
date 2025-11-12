package store.bookscamp.api.orderinfo.service.dto;

public record OrderItemCreateDto(
        Long bookId,
        Integer quantity,
        Long packagingId
) {
}