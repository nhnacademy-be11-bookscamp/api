package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;

public record BookDetailDto(

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
    public static BookDetailDto from(Book book) {
        return new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getExplanation(),
                book.getContent(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getContributors(),
                book.getStatus(),
                book.isPackable(),
                book.getRegularPrice(),
                book.getSalePrice(),
                book.getStock(),
                book.getViewCount()
        );
    }
}
