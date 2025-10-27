package store.bookscamp.api.cart.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequest(

        @Min(value = 1)
        @NotNull
        Integer quantity
) {
}
