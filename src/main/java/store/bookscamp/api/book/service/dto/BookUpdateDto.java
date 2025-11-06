package store.bookscamp.api.book.service.dto;

import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.BookStatus;

import java.time.LocalDate;
import java.util.List;

public record BookUpdateDto(
        String title,
        String contributors,
        String publisher,
        String isbn,
        LocalDate publishDate,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        boolean packable,
        String content,
        String explanation,
        List<String> imageUrls,
        List<String> removedUrls,
        List<Long> tagIds,
        Long categoryId,
        BookStatus status
) {
    public static BookUpdateDto from(BookUpdateRequest req) {
        return new BookUpdateDto(
                req.getTitle(),
                req.getContributors(),
                req.getPublisher(),
                req.getIsbn(),
                req.getPublishDate(),
                req.getRegularPrice(),
                req.getSalePrice(),
                req.getStock(),
                req.isPackable(),
                req.getContent(),
                req.getExplanation(),
                req.getImageUrls(),
                req.getRemovedUrls(),
                req.getTagIds(),
                req.getCategoryId(),
                req.getStatus()
        );
    }
}
