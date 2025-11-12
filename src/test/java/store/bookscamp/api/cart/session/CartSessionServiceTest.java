package store.bookscamp.api.cart.session;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class CartSessionServiceTest {

    @Autowired
    private CartSessionService cartSessionService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("비회원은 새로운 장바구니가 생성되어 세션에 저장된다")
    void guestCreateCartAndSession() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Long cartId = cartSessionService.extractCartId(request);

        // then
        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("cartId")).isEqualTo(cartId);
        assertThat(cartRepository.findById(cartId)).isPresent();
        assertThat(cartRepository.findById(cartId).get().getMember()).isNull();
    }

    @Test
    @DisplayName("회원은 기존 장바구니가 없으면 새로 생성되고 세션에 저장된다")
    void memberCreateCartAndSession() {
        // given
        Member member = memberRepository.save(new Member(
                "회원",
                "1234",
                "member@naver.com",
                "01012345678",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member",
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 1)
        ));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-USER-ID", member.getId().toString());

        // when
        Long cartId = cartSessionService.extractCartId(request);

        // then
        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("cartId")).isEqualTo(cartId);

        Cart cart = cartRepository.findById(cartId).orElseThrow();
        assertThat(cart.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("세션에 cartId가 있으면 기존 cartId를 그대로 반환한다")
    void reuseExistingSessionCart() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute("cartId", 99L);

        // when
        Long result = cartSessionService.extractCartId(request);

        // then
        assertThat(result).isEqualTo(99L);
    }
}
