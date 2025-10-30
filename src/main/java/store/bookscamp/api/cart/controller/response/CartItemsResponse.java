package store.bookscamp.api.cart.controller.response;

import store.bookscamp.api.cart.service.dto.CartItemDto;

public record CartItemsResponse(
        Long cartItemId
) {
    public static CartItemsResponse from(CartItemDto dto) {
        return new CartItemsResponse(dto.cartItemId());
    }
}
