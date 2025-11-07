package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import java.util.List;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookDetailDto;

public record BookInfoResponse(
        Long id,
        String title,
        String explanation,
        String content,
        String publisher,
        LocalDate publishDate,
        String contributors,
        String isbn,
        BookStatus status,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        long viewCount,
        Long categoryId,
        List<Long> tagIds,
        List<String> imageUrls
) {
    public static BookInfoResponse from(BookDetailDto dto) {
        return new BookInfoResponse(
                dto.id(),
                dto.title(),
                dto.explanation(),
                dto.content(),
                dto.publisher(),
                dto.publishDate(),
                dto.contributors(),
                dto.isbn(),
                dto.status(),
                dto.packable(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.stock(),
                dto.viewCount(),
                dto.categoryId(),
                dto.tagIds(),
                dto.imageUrls()
        );
    }
}
