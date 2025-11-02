package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookSortDto;

public record BookSortResponse(
        Long id,
        String title,
        String explanation,
        String content,
        String publisher,
        LocalDate publishDate,
        String contributors,
        BookStatus status,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        long viewCount
) {

    public static BookSortResponse from(BookSortDto bookSortDto) {
        return new BookSortResponse(
                bookSortDto.id(),
                bookSortDto.title(),
                bookSortDto.explanation(),
                bookSortDto.content(),
                bookSortDto.publisher(),
                bookSortDto.publishDate(),
                bookSortDto.contributors(),
                bookSortDto.status(),
                bookSortDto.packable(),
                bookSortDto.regularPrice(),
                bookSortDto.salePrice(),
                bookSortDto.stock(),
                bookSortDto.viewCount()
        );
    }
}
