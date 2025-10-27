package store.bookscamp.api.cart.service;

import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;
import store.bookscamp.api.cart.service.dto.CartItemDto;
import store.bookscamp.api.common.exception.ApplicationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;

    private static final String CART_PREFIX = "cart:";
    private final RedisTemplate<String, Object> redisTemplate;

    private final CartAsyncService cartAsyncService;

    public Long addCartItem(CartItemAddDto dto) {
        Cart cart = cartRepository.findById(dto.cartId())
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));
        CartItem cartItem = new CartItem(cart, book, dto.quantity());
        Long cartItemId = cartItemRepository.save(cartItem).getId();
        redisTemplate.opsForHash().putIfAbsent(CART_PREFIX + cart.getId(), cartItemId, cartItem.getQuantity());

        return cartItemId;
    }

    public void updateCart(Long cartId, Long cartItemId, Integer quantity) {
        redisTemplate.opsForHash().put(CART_PREFIX + cartId, cartItemId, quantity);
        cartAsyncService.updateCartAsync(cartItemId, quantity);
    }

    public void deleteCartItem(Long cartId, Long cartItemId) {
        Long result = redisTemplate.opsForHash().delete(CART_PREFIX + cartId, cartItemId);
        if (result == 0) {
            throw new ApplicationException(CART_ITEM_NOT_FOUND);
        }
        cartAsyncService.deleteCartItemAsync(cartItemId);
    }

    public void clearCart(Long cartId) {
        Boolean result = redisTemplate.delete(CART_PREFIX + cartId);
        if (!result) {
            log.info("cart clear: cache에 존재하지 않는 cart. cartId = {}", cartId);
        }
        cartAsyncService.clearCartAsync(cartId);
    }

    public List<CartItemDto> getCartItems(Long cartId) {
        return redisTemplate.opsForHash().entries(CART_PREFIX + cartId)
                .entrySet().stream()
                .map((it) -> new CartItemDto(
                        Long.parseLong(it.getKey().toString()), Integer.parseInt(it.getValue().toString())))
                .toList();
    }
}
