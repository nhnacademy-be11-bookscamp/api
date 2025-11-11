package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import lombok.Builder;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.entity.BookStatus;


@Builder
public record BookSortDto(
        Long id,
        String title,
        String publisher,
        LocalDate publishDate,
        String contributors,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        long viewCount,
        String isbn,
        double averageRating,
        long reviewCount
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
                book.getStock(),
                book.getViewCount(),
                null,
                0.0,
                0L
        );
    }

    public static BookSortDto fromDocument(BookDocument doc) {
        return BookSortDto.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .publisher(doc.getPublisher())
                .publishDate(doc.getPublishDate())
                .contributors(doc.getContributors())
                .salePrice(doc.getSalePrice())
                .stock(doc.getStock())
                .viewCount(doc.getViewCount())
                .isbn(doc.getIsbn())
                .averageRating(doc.getAverageRating())
                .reviewCount(doc.getReviewCount())
                .packable(doc.isPackable())
                .regularPrice(doc.getRegularPrice())
                .build();
    }
}

