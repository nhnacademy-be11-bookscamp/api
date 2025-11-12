package store.bookscamp.api.orderinfo.service.dto;

public record OrderItemDto(
        Long bookId,
        String bookTitle,
        String bookImageUrl,
        Integer bookPrice,
        Integer quantity,
        Integer bookTotalAmount,
        Boolean packagingAvailable
) {
}