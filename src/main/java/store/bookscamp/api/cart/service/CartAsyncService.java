package store.bookscamp.api.cart.service;

import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
    @Retryable(noRetryFor = ApplicationException.class, backoff = @Backoff(multiplier = 2.0, maxDelay = 10000), listeners = "customRetryListener")
    @Transactional
    public void updateCartAsync(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUND));
        cartItem.updateQuantity(quantity);
    }

    @Async("cartExecutor")
    @Retryable(noRetryFor = ApplicationException.class, backoff = @Backoff(multiplier = 2.0, maxDelay = 10000), listeners = "customRetryListener")
    public void deleteCartItemAsync(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUND));
        cartItemRepository.delete(cartItem);
    }

    @Async("cartExecutor")
    @Retryable(noRetryFor = ApplicationException.class, backoff = @Backoff(multiplier = 2.0, maxDelay = 10000), listeners = "customRetryListener")
    @Transactional
    public void clearCartAsync(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
        cartItemRepository.deleteAllByCart(cart);
    }
}
