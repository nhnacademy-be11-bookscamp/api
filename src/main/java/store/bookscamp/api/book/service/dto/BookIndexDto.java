package store.bookscamp.api.book.service.dto;

public record BookIndexDto(

        Long id,
        String title,
        String publisher,
        String contributors,
        Integer regularPrice,
        Integer salePrice,
        String thumbnail
) {
}
