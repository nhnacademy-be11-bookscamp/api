package store.bookscamp.api.book.service.dto;

import store.bookscamp.api.book.entity.Book;

public record BookIndexDto(

        Long id,
        String title,
        String publisher,
        String contributors,
        Integer regularPrice,
        Integer salePrice,
        String thumbnail
) {
    public static BookIndexDto from(Book book, String thumbnailUrl){
        return new BookIndexDto(
                book.getId(),
                book.getTitle(),
                book.getPublisher(),
                book.getContributors(),
                book.getRegularPrice(),
                book.getSalePrice(),
                thumbnailUrl
        );
    }
}
