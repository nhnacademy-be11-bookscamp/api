package store.bookscamp.api.cart.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Disabled
@SpringBootTest
@Transactional
class CartCookieServiceTest {

    @Autowired
    private CartCookieService cartCookieService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CART_COOKIE = "cartToken";
    private static final String CART_ID_PREFIX = "cartId:";

    @Test
    @DisplayName("비회원 요청 시 새로운 장바구니 생성 후 쿠키 발급 및 Redis 저장")
    void guestCreateCartAndCookie() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        Long cartId = cartCookieService.extractCartId(request, response);

        // then
        // 쿠키 발급 확인
        String setCookieHeader = response.getHeader("Set-Cookie");
        assertThat(setCookieHeader).contains(CART_COOKIE);
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("Secure");

        // Redis 저장 확인
        String token = setCookieHeader.split(CART_COOKIE + "=")[1].split(";")[0];
        String redisCartId = redisTemplate.opsForValue().get(CART_ID_PREFIX + token);
        assertThat(Long.parseLong(redisCartId)).isEqualTo(cartId);

        // DB 저장 확인
        Cart savedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(savedCart.getMember()).isNull();
    }

    @Test
    @DisplayName("비회원이 쿠키를 가지고 재요청하면 기존 Redis 매핑 카트 ID를 그대로 사용한다")
    void reuseExistingGuestCart() {
        // given
        Long cartId = cartRepository.save(new Cart(null)).getId();
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CART_ID_PREFIX + token, cartId.toString());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CART_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        Long result = cartCookieService.extractCartId(request, response);

        // then
        assertThat(result).isEqualTo(cartId);
    }

    @Test
    @DisplayName("회원 요청 시 기존 카트가 없으면 새로 생성되고 해당 회원과 연결된다")
    void memberCreateCart() {
        // given
        Member member = memberRepository.save(new Member(
                "회원",
                "1234",
                "member@naver.com",
                "01012345678",
                0,
                NORMAL,
                LocalDate.now(),
                "member",
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 1)
        ));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-USER-ID", member.getId().toString());

        // when
        Long cartId = cartCookieService.extractCartId(request, response);

        // then
        Cart savedCart = cartRepository.findById(cartId).orElseThrow();
        assertThat(savedCart.getMember().getId()).isEqualTo(member.getId());
    }
}
