package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import java.util.List;
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
    public static BookDetailDto from(
            Book book,
            Long categoryId,
            List<Long> tagIds,
            List<String> imageUrls
    ) {
        return new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getExplanation(),
                book.getContent(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getContributors(),
                book.getIsbn(),
                book.getStatus(),
                book.isPackable(),
                book.getRegularPrice(),
                book.getSalePrice(),
                book.getStock(),
                book.getViewCount(),
                categoryId,
                tagIds,
                imageUrls
        );
    }
}
