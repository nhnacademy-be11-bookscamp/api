package store.bookscamp.api.cart.service.dto;

public record CartItemDto(
        Long cartItemId,
        Integer quantity
) {
}
