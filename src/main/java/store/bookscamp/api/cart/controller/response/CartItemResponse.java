package store.bookscamp.api.cart.controller.response;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.cart.entity.CartItem;

public record CartItemResponse(
        Book book,
        Integer quantity
) {

    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(cartItem.getBook(), cartItem.getQuantity());
    }
}
