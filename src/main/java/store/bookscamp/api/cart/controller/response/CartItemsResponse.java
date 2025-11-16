package store.bookscamp.api.cart.controller.response;

import store.bookscamp.api.cart.service.dto.CartItemDto;

public record CartItemsResponse(
        Long cartItemId,
        Long bookId,
        String bookTitle,
        String thumbnailImageUrl,
        Integer quantity,
        Integer regularPrice,
        Integer salePrice,
        Integer totalPrice
) {
    public static CartItemsResponse from(CartItemDto dto) {
        return new CartItemsResponse(
                dto.getCartItemId(),
                dto.getBookId(),
                dto.getBookTitle(),
                dto.getThumbnailImageUrl(),
                dto.getQuantity(),
                dto.getRegularPrice(),
                dto.getSalePrice(),
                dto.getTotalPrice()
        );
    }
}
