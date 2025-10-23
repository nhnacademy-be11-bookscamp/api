package store.bookscamp.api.cart.controller;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.cart.controller.request.CartItemAddRequest;
import store.bookscamp.api.cart.controller.request.CartItemUpdateRequest;
import store.bookscamp.api.cart.service.CartService;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCartItem(
            @CookieValue(name = "cartId", required = false) Long cartId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        ResponseCookie cookie = null;
        if (cartId == null) {
            cartId = cartService.createCart(request.memberId());

            cookie = ResponseCookie.from("cartId", cartId.toString())
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .build();
        }

        CartItemAddDto dto = request.toDto(cartId);
        cartService.addCartItem(dto);

        if (cookie != null) {
            return ResponseEntity.ok()
                    .header(SET_COOKIE, cookie.toString())
                    .build();
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        cartService.updateCart(cartItemId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }
}
