package store.bookscamp.api.cart.service.dto;

public record CartItemAddDto(
        Long cartId,
        Long memberId,
        Long bookId,
        Integer quantity
) {
}
