package store.bookscamp.api.book.controller.response;

import store.bookscamp.api.book.entity.Book;

public record BookCouponResponse(

        Long id,
        String title
) {
    public static BookCouponResponse from(Book book){
        return new BookCouponResponse(
                book.getId(),
                book.getTitle()
        );
    }
}
