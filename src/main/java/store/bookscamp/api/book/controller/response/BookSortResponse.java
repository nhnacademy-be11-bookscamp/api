package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookSortDto;

public record BookSortResponse(
        
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
        long reviewCount,
        String aiRecommand,
        String thumbnailUrl,
        Integer aiRank
) {

    public static BookSortResponse from(BookSortDto bookSortDto, String thumbnailUrl) {
        return new BookSortResponse(
                bookSortDto.getId(),
                bookSortDto.getTitle(),
                bookSortDto.getPublisher(),
                bookSortDto.getPublishDate(),
                bookSortDto.getContributors(),
                bookSortDto.isPackable(),
                bookSortDto.getRegularPrice(),
                bookSortDto.getSalePrice(),
                bookSortDto.getStock(),
                bookSortDto.getViewCount(),
                bookSortDto.getIsbn(),
                bookSortDto.getAverageRating(),
                bookSortDto.getReviewCount(),
                bookSortDto.getAiRecommand(),
                thumbnailUrl,
                bookSortDto.getAiRank()
        );
    }
}
