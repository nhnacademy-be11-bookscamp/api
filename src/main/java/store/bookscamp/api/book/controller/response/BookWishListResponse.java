package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookWishListDto;

public record BookWishListResponse(

        Long id,
        String title,
        String publisher,
        LocalDate publishDate,
        String contributors,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        BookStatus status,
        String thumbnailUrl
) {
    public static BookWishListResponse from(BookWishListDto bookWishListDto) {
        return new BookWishListResponse(
                bookWishListDto.id(),
                bookWishListDto.title(),
                bookWishListDto.publisher(),
                bookWishListDto.publishDate(),
                bookWishListDto.contributors(),
                bookWishListDto.packable(),
                bookWishListDto.regularPrice(),
                bookWishListDto.salePrice(),
                bookWishListDto.status(),
                bookWishListDto.thumbnailUrl()
        );
    }
}
