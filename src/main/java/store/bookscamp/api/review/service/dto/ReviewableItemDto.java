package store.bookscamp.api.review.service.dto;

public record ReviewableItemDto(
        Long orderItemId,
        Long bookId,
        String bookTitle,
        String thumbnailUrl
) {}