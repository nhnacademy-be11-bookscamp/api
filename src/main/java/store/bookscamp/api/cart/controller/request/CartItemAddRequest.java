package store.bookscamp.api.cart.controller.request;

import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;

public record CartItemAddRequest(

        @NotNull
        Long bookId,

        @NotNull
        Integer quantity
) {

    public CartItemAddDto toDto(Long cartId) {
        return new CartItemAddDto(
                cartId,
                bookId,
                quantity
        );
    }
}
