package store.bookscamp.api.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.common.config.JpaConfig;
import store.bookscamp.api.contributor.entity.Contributor;
import store.bookscamp.api.contributor.repository.ContributorRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;

@Import(JpaConfig.class)
@DataJpaTest
class CartItemRepositoryTest {

    @MockitoBean
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ContributorRepository contributorRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Cart cart;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        Contributor contributor = contributorRepository.save(new Contributor("저자"));
        member = memberRepository.save(new Member(
                "회원",
                "1234",
                "test@naver.com",
                "01012345678",
                0,
                MemberStatus.NORMAL,
                LocalDate.now(),
                "member",
                LocalDateTime.now(),
                LocalDate.of(2000, 1, 1)
        ));
        cart = cartRepository.save(new Cart(member));

        book1 = bookRepository.save(new Book(
                "도서1",
                "설명",
                null,
                "출판사",
                LocalDate.of(2000, 1, 1),
                "1234567890",
                contributor,
                BookStatus.AVAILABLE,
                false,
                10000,
                9000,
                10,
                0L
        ));

        book2 = bookRepository.save(new Book(
                "도서2",
                "설명2",
                null,
                "출판사2",
                LocalDate.of(2000, 1, 1),
                "1234567891",
                contributor,
                BookStatus.AVAILABLE,
                false,
                20000,
                18000,
                20,
                0L
        ));
    }

    @Test
    @DisplayName("findAllByCart - 장바구니에 담긴 모든 아이템 조회")
    void findAllByCart_success() {
        // given
        cartItemRepository.save(new CartItem(cart, book1, 1));
        cartItemRepository.save(new CartItem(cart, book2, 2));

        // when
        List<CartItem> items = cartItemRepository.findAllByCart(cart);

        // then
        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(item -> item.getBook().getTitle())
                .containsExactlyInAnyOrder("도서1", "도서2");
    }

    @Test
    @DisplayName("deleteAllByCart - 장바구니 비우기")
    void deleteAllByCart_success() {
        // given
        cartItemRepository.save(new CartItem(cart, book1, 1));
        cartItemRepository.save(new CartItem(cart, book2, 2));

        // when
        cartItemRepository.deleteAllByCart(cart);

        // then
        List<CartItem> remaining = cartItemRepository.findAllByCart(cart);
        assertThat(remaining).isEmpty();
    }
}
