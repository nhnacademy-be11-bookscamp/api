package store.bookscamp.api.cart.session;

import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartSessionService {

    private static final String CART_ID_KEY = "cartId";

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;

    public Long extractCartId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Long cartId = (Long) session.getAttribute(CART_ID_KEY);
        if (cartId != null) {
            return cartId;
        }

        log.info("세션 cartId가 null입니다.");
        Long newCartId = createOrGetCart(request.getHeader("X-USER-ID"));
        session.setAttribute(CART_ID_KEY, newCartId);
        return newCartId;
    }

    private Long createOrGetCart(String header) {
        if (header != null) {
            Long memberId = Long.parseLong(header);
            return cartRepository.findByMemberId(memberId)
                    .orElseGet(() -> {
                        log.info("새 카트 생성. memberId = {}", memberId);
                        Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
                        return cartRepository.save(new Cart(member));
                    }).getId();
        }

        Long cartId = cartRepository.save(new Cart(null)).getId();
        log.info("비회원 카트 생성. cartId = {}", cartId);
        return cartId;
    }
}