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
        long viewCount,
        String isbn,
        double averageRating,
        long reviewCount
) {

    public static BookSortDto from(Book book) {
        return new BookSortDto(
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
                .explanation(doc.getExplanation())
                .content(doc.getContent())
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
                .status(null) // ES에는 enum이 없을 수 있으니 null로
                .regularPrice(doc.getRegularPrice())
                .build();
    }
}

