package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.OrderItemDto;

public record OrderItemInfo(
        Long bookId,
        String bookTitle,
        String bookImageUrl,
        Integer bookPrice,
        Integer quantity,
        Integer bookTotalAmount,
        Boolean packagingAvailable
) {
    public static OrderItemInfo fromDto(OrderItemDto dto) {
        return new OrderItemInfo(
                dto.bookId(),
                dto.bookTitle(),
                dto.bookImageUrl(),
                dto.bookPrice(),
                dto.quantity(),
                dto.bookTotalAmount(),
                dto.packagingAvailable()
        );
    }
}