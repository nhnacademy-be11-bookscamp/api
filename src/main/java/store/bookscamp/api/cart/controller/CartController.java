package store.bookscamp.api.cart.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.cart.controller.request.CartItemAddRequest;
import store.bookscamp.api.cart.controller.request.CartItemUpdateRequest;
import store.bookscamp.api.cart.controller.response.CartItemsResponse;
import store.bookscamp.api.cart.service.CartService;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;
import store.bookscamp.api.cart.session.CartId;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "Cart API입니다")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCartItem(
            @CartId Long cartId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemAddDto dto = request.toDto(cartId);
        cartService.addCartItem(dto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @CartId Long cartId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        cartService.updateCart(cartId, cartItemId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @CartId Long cartId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(cartId, cartItemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemsResponse>> getCartItems(
            @CartId Long cartId
    ) {
        List<CartItemsResponse> response = cartService.getCartItems(cartId).stream()
                .map(CartItemsResponse::from)
                .toList();
        return ResponseEntity.ok().body(response);
    }
}
