package store.bookscamp.api.cart.service.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CartItemDto {
    Long cartItemId;
    Long bookId;
    String thumbnailImageUrl;
    Integer quantity;
    Integer regularPrice;
    Integer salePrice;
    Integer totalPrice;

    @QueryProjection
    public CartItemDto(
            Long cartItemId,
            Long bookId,
            String thumbnailImageUrl,
            Integer quantity,
            Integer regularPrice,
            Integer salePrice,
            Integer totalPrice
    ) {
        this.cartItemId = cartItemId;
        this.bookId = bookId;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.quantity = quantity;
        this.regularPrice = regularPrice;
        this.salePrice = salePrice;
        this.totalPrice = totalPrice;
    }
}