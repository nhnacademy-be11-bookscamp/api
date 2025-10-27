package store.bookscamp.api.cart.service;

import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.common.exception.ApplicationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartAsyncService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @Async("cartExecutor")
    @Transactional
    public void updateCartAsync(Long cartItemId, Integer quantity) {
        try {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUND));
            cartItem.updateQuantity(quantity);
        } catch (Exception e) {
            log.error("cartItem update 오류. cartItemId = {}", cartItemId, e);
        }
    }

    @Async("cartExecutor")
    public void deleteCartItemAsync(Long cartItemId) {
        try {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUND));
            cartItemRepository.delete(cartItem);
        } catch (Exception e) {
            log.error("cartItem delete 오류. cartItemId = {}", cartItemId, e);
        }
    }

    @Async("cartExecutor")
    @Transactional
    public void clearCartAsync(Long cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
            cartItemRepository.deleteAllByCart(cart);
        } catch (Exception e) {
            log.error("clear cart 오류. cartId = {}", cartId, e);
        }
    }
}
