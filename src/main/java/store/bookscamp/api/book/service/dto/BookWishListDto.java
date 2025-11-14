package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import store.bookscamp.api.book.entity.BookStatus;

public record BookWishListDto(

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

}
