package store.bookscamp.api.cart.service;

import static java.lang.Boolean.FALSE;
import static java.time.Duration.ofDays;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.query.CartItemSearchQuery;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;
import store.bookscamp.api.cart.service.dto.CartItemDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    private static final String CART_PREFIX = "cart:";
    private static final Integer CART_CACHE_TTL = 7;
    private final RedisTemplate<String, Object> redisTemplate;

    private final CartItemSearchQuery cartItemSearchQuery;
    private final CartAsyncService cartAsyncService;

    @Transactional
    public Long addCartItem(CartItemAddDto dto) {
        Cart cart = cartRepository.findById(dto.cartId())
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));
        CartItem cartItem = new CartItem(cart, book, dto.quantity());
        Long cartItemId = cartItemRepository.save(cartItem).getId();
        redisTemplate.opsForHash().put(CART_PREFIX + cart.getId(), cartItemId.toString(), cartItem.getQuantity());

        return cartItemId;
    }

    public void updateCart(Long cartId, Long cartItemId, Integer quantity) {
        redisTemplate.opsForHash().put(CART_PREFIX + cartId, cartItemId.toString(), quantity);

        try {
            cartAsyncService.updateCartAsync(cartItemId, quantity);
        } catch (Exception e) {
            log.error("cartItem update 오류. cartItemId = {}", cartItemId, e);
        }
    }

    public void deleteCartItem(Long cartId, Long cartItemId) {
        Long result = redisTemplate.opsForHash().delete(CART_PREFIX + cartId, cartItemId.toString());
        if (result == 0) {
            throw new ApplicationException(CART_ITEM_NOT_FOUND);
        }

        try {
            cartAsyncService.deleteCartItemAsync(cartItemId);
        } catch (Exception e) {
            log.error("cartItem delete 오류. cartItemId = {}", cartItemId, e);
        }
    }

    public void clearCart(Long cartId) {
        Boolean result = redisTemplate.delete(CART_PREFIX + cartId);
        if (!result) {
            log.info("cart clear: cache에 존재하지 않는 cart. cartId = {}", cartId);
        }

        try {
            cartAsyncService.clearCartAsync(cartId);
        } catch (Exception e) {
            log.error("clear cart 오류. cartId = {}", cartId, e);
        }
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long cartId) {
        String key = CART_PREFIX + cartId;
        if (FALSE.equals(redisTemplate.hasKey(key)) || cartItemRepository.countCartItemByCartId(cartId) != redisTemplate.opsForHash().size(key)) {
            return fallback(cartId);
        }
        return redisTemplate.opsForHash().entries(key)
                .keySet().stream()
                .map(k -> cartItemSearchQuery.searchCartItemById(Long.parseLong(k.toString())))
                .toList();
    }

    private List<CartItemDto> fallback(Long cartId) {
        String key = CART_PREFIX + cartId;

        List<CartItemDto> dtos = cartItemSearchQuery.searchCartItemsByCartId(cartId);

        Map<String, Integer> map = new HashMap<>();
        for (CartItemDto dto : dtos) {
            map.put(dto.getCartItemId().toString(), dto.getQuantity());
        }
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, map);
        log.info("cart cache fallback. cartId = {}", cartId);

        return dtos;
    }

    public void extendCacheTtl(Long cartId) {
        redisTemplate.expire(CART_PREFIX + cartId, ofDays(CART_CACHE_TTL));
    }

    @Transactional
    public Long createOrGetCart(Long memberId) {
        if (memberId == null) {
            return cartRepository.save(new Cart(null)).getId();
        }

        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    log.info("새 카트 생성. memberId = {}", memberId);
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
                    return cartRepository.save(new Cart(member));
                }).getId();
    }
}