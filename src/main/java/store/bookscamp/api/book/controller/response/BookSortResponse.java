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
        String thumbnailUrl
) {

    public static BookSortResponse from(BookSortDto bookSortDto, String thumbnailUrl) {
        return new BookSortResponse(
                bookSortDto.id(),
                bookSortDto.title(),
                bookSortDto.publisher(),
                bookSortDto.publishDate(),
                bookSortDto.contributors(),
                bookSortDto.packable(),
                bookSortDto.regularPrice(),
                bookSortDto.salePrice(),
                bookSortDto.stock(),
                thumbnailUrl
        );
    }
}
