package store.bookscamp.api.cart.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Disabled
class CartSessionRedisIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void sessionIsCreatedInRedis() {
        // when
        restTemplate.postForEntity("/cart/test-session", null, Void.class);

        // then
        Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
        assertThat(keys).isNotEmpty();
    }
}


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartTestController {

    private final CartSessionService cartSessionService;

    @PostMapping("/test-session")
    public ResponseEntity<Void> testSession(HttpServletRequest request) {
        cartSessionService.extractCartId(request);
        return ResponseEntity.ok().build();
    }
}
