package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.contributor.entity.Contributor;

public record BookSortDto(
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
    public static BookSortDto from(Book book) {
        return new BookSortDto(
                book.getId(),
                book.getTitle(),
                book.getExplanation(),
                book.getContent(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getContributor(),
                book.getStatus(),
                book.getPackable(),
                book.getRegularPrice(),
                book.getSalePrice(),
                book.getStock(),
                book.getViewCount()
        );
    }
}
