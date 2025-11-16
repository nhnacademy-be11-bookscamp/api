package store.bookscamp.api.cart.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Disabled
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CartCookieRedisIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CART_COOKIE = "cartToken";
    private static final String CART_ID_PREFIX = "cartId:";

    @Test
    @DisplayName("비회원 요청 시 cartToken 쿠키가 발급되고 Redis에 cartId 매핑이 저장된다")
    void createCartTokenAndRedisMapping() {
        // when
        ResponseEntity<Void> response = restTemplate.postForEntity("/cart/test-cookie", null, Void.class);

        // then: 쿠키 발급 확인
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains(CART_COOKIE);

        // cartToken 추출
        String cartToken = setCookieHeader.split(CART_COOKIE + "=")[1].split(";")[0];

        // Redis 키 검증
        Set<String> keys = redisTemplate.keys(CART_ID_PREFIX + "*");
        assertThat(keys).anyMatch(k -> k.contains(cartToken));

        // Redis value 검증
        String cartId = redisTemplate.opsForValue().get(CART_ID_PREFIX + cartToken);
        assertThat(cartId).isNotNull();
    }
}

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
class CartTestController {

    private final CartCookieService cartCookieService;

    @PostMapping("/test-cookie")
    public ResponseEntity<Void> testCookie(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        cartCookieService.extractCartId(request, response);
        return ResponseEntity.ok().build();
    }
}
