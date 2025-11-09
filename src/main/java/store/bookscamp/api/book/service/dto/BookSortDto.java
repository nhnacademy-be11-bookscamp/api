package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;

public record BookSortDto(

        Long id,
        String title,
        String publisher,
        LocalDate publishDate,
        String contributors,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock
) {
    public static BookSortDto from(Book book) {
        return new BookSortDto(
                book.getId(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getContributors(),
                book.isPackable(),
                book.getRegularPrice(),
                book.getSalePrice(),
                book.getStock()
        );
    }
}
