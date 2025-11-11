package store.bookscamp.api.book.controller.response;

import store.bookscamp.api.book.service.dto.BookIndexDto;

public record BookIndexResponse(
        Long id,
        String title,
        String publisher,
        String contributors,
        Integer regularPrice,
        Integer salePrice,
        String thumbnail
) {

    public static BookIndexResponse from(BookIndexDto dto){
        return new BookIndexResponse(
                dto.id(),
                dto.title(),
                dto.publisher(),
                dto.contributors(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.thumbnail()
        );
    }
}
