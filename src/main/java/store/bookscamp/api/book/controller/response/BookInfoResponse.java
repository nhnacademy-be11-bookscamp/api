package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookDetailDto;

public record BookInfoResponse(

        Long id,
        String title,
        String explanation,
        String content,
        String publisher,
        LocalDate publishDate,
        BookStatus status,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        long viewCount
){
    public static BookInfoResponse from(BookDetailDto dto){
        return new BookInfoResponse(
                dto.id(),
                dto.title(),
                dto.explanation(),
                dto.content(),
                dto.publisher(),
                dto.publishDate(),
                dto.status(),
                dto.packable(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.stock(),
                dto.viewCount()
        );
    }
}
