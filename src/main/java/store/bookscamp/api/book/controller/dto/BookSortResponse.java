package store.bookscamp.api.book.controller.dto;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.contributor.entity.Contributor;

public record BookSortResponse(
        Long id,
        String title,
        String explanation,
        String content,
        String publisher,
        LocalDate publishDate,
        Contributor contributor,
        BookStatus status,
        Boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        Long viewCount
) {
}
