package store.bookscamp.api.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class CartAsyncServiceTest {

    @Autowired
    private CartAsyncService cartAsyncService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member(
                "회원",
                "1234",
                "member@naver.com",
                "01012345678",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member_" + System.nanoTime(),
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 1)
        ));

        cart = cartRepository.save(new Cart(member));
        Book book = bookRepository.save(new Book(
                "책 제목",
                "책 설명",
                null,
                "출판사",
                LocalDate.of(2001, 1, 1),
                "123456789012",
                "기여자",
                AVAILABLE,
                false,
                20000,
                18000,
                100,
                0L
        ));
        cartItem = cartItemRepository.save(new CartItem(cart, book, 2));
    }

    @AfterEach
    void afterEach() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("비동기 - 장바구니 아이템 수량 수정 성공")
    void updateCartAsync_success() throws Exception {
        // when
        cartAsyncService.updateCartAsync(cartItem.getId(), 10);

        // then (비동기 완료 대기)
        TimeUnit.MILLISECONDS.sleep(100);
        CartItem updated = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("비동기 - 장바구니 아이템 삭제 성공")
    void deleteCartItemAsync_success() throws Exception {
        // when
        cartAsyncService.deleteCartItemAsync(cartItem.getId());

        // then
        TimeUnit.MILLISECONDS.sleep(100);
        assertThat(cartItemRepository.findById(cartItem.getId())).isEmpty();
    }

    @Test
    @DisplayName("비동기 - 장바구니 비우기 성공")
    void clearCartAsync_success() throws Exception {
        // when
        cartAsyncService.clearCartAsync(cart.getId());

        // then
        TimeUnit.MILLISECONDS.sleep(100);
        assertThat(cartItemRepository.findAllByCartId(cart.getId())).isEmpty();
    }
}
