package store.bookscamp.api.cart.cookie;

import static java.time.Duration.ofDays;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import store.bookscamp.api.cart.service.CartService;
import store.bookscamp.api.common.exception.ApplicationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCookieService {

    private static final String CART_ID_PREFIX = "cartId:";
    private static final Integer CART_ID_CACHE_TTL = 7;
    private static final String CART_COOKIE = "cartToken";

    private final RedisTemplate<String, String> redisTemplate;
    private final CartService cartService;

    public Long extractCartId(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader("X-User-ID");
        if (header != null) {
            Long cartId = cartService.createOrGetCart(Long.parseLong(header));
            cartService.extendCacheTtl(cartId);
            return cartId;
        }

        Cookie cookie = WebUtils.getCookie(request, CART_COOKIE);
        if (cookie != null) {
            String key = CART_ID_PREFIX + cookie.getValue();
            String cartId = redisTemplate.opsForValue().get(key);
            redisTemplate.expire(key, ofDays(CART_ID_CACHE_TTL));
            setCookie(response, cookie.getValue());
            if (cartId == null) {
                log.info("존재하지 않는 redis cartID key. key = {}", key);
                throw new ApplicationException(CART_NOT_FOUND);
            }
            cartService.extendCacheTtl(Long.parseLong(cartId));
            return Long.parseLong(cartId);
        }

        Long cartId = cartService.createOrGetCart(null);
        String cartToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CART_ID_PREFIX + cartToken, cartId.toString(), ofDays(7));
        cartService.extendCacheTtl(cartId);
        log.info("비회원 카트 생성. cartId = {}", cartId);
        setCookie(response, cartToken);

        return cartId;
    }

    private static void setCookie(HttpServletResponse response, String cartToken) {
        ResponseCookie responseCookie = ResponseCookie.from(CART_COOKIE, cartToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(ofDays(7))
                .build();
        response.addHeader(SET_COOKIE, responseCookie.toString());
    }
}