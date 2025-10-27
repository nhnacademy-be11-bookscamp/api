package store.bookscamp.api.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;
import store.bookscamp.api.cart.service.dto.CartItemDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.contributor.entity.Contributor;
import store.bookscamp.api.contributor.repository.ContributorRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@SpringBootTest
@Transactional
@Disabled
class CartServiceTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ContributorRepository contributorRepository;

    private Contributor contributor;
    private Member member;
    private Cart cart;
    private Book book;

    @BeforeEach
    void setUp() {
        contributor = contributorRepository.save(new Contributor("기여자"));
        member = memberRepository.save(new Member(
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
        cart = cartRepository.save(new Cart(member));
        book = bookRepository.save(new Book(
                "책 제목",
                "책 설명",
                null,
                "출판사",
                LocalDate.of(2001, 1, 1),
                "123456789012",
                contributor,
                AVAILABLE,
                false,
                20000,
                18000,
                100,
                0L
        ));
    }

    @Nested
    @DisplayName("장바구니에 물품을 추가할 때")
    class AddCartItemTest {

        @Test
        @DisplayName("정상적으로 장바구니 아이템 추가")
        void success_addCartItem() {
            // given
            CartItemAddDto dto = new CartItemAddDto(cart.getId(), book.getId(), 3);

            // when
            Long cartItemId = cartService.addCartItem(dto);

            // then
            CartItem savedItem = cartItemRepository.findById(cartItemId).orElseThrow();
            assertThat(savedItem.getQuantity()).isEqualTo(3);
            assertThat(savedItem.getCart().getId()).isEqualTo(cart.getId());
            assertThat(savedItem.getBook().getId()).isEqualTo(book.getId());
        }

        @Test
        @DisplayName("존재하지 않는 장바구니 ID일 경우 예외 발생")
        void fail_cartNotFound() {
            CartItemAddDto dto = new CartItemAddDto(999L, book.getId(), 1);

            assertThatThrownBy(() -> cartService.addCartItem(dto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(CART_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 도서 ID일 경우 예외 발생")
        void fail_bookNotFound() {
            CartItemAddDto dto = new CartItemAddDto(cart.getId(), 999L, 1);

            assertThatThrownBy(() -> cartService.addCartItem(dto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(BOOK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 물품 수량 수정 시")
    class UpdateCartTest {

        @Test
        @DisplayName("성공")
        void updateCart_success() {
            // given
            CartItemAddDto dto = new CartItemAddDto(cart.getId(), book.getId(), 3);
            Long cartItemId = cartService.addCartItem(dto);

            // when
            cartService.updateCart(cart.getId(), cartItemId,5);

            // then
            List<CartItemDto> cartItems = cartService.getCartItems(cart.getId());
            for (CartItemDto cartItem : cartItems) {
                if (cartItem.cartItemId().equals(cartItemId)) {
                    assertThat(cartItem.quantity()).isEqualTo(5);
                }
            }
        }
    }

    @Nested
    @DisplayName("장바구니 물품 삭제 시")
    class DeleteCartItemTest {

        @Test
        @DisplayName("장바구니 물품 삭제 성공")
        void deleteCartItem_success() {
            // given
            CartItemAddDto dto = new CartItemAddDto(cart.getId(), book.getId(), 1);
            Long itemId = cartService.addCartItem(dto);

            // when
            cartService.deleteCartItem(cart.getId(), itemId);

            // then
            assertThat(cartService.getCartItems(cart.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 CartItem 삭제 시 예외 발생")
        void deleteCartItem_notFound() {
            assertThatThrownBy(() -> cartService.deleteCartItem(cart.getId(), 999L))
                    .isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(CART_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 비우기 시")
    class clearCartTest {

        @Test
        void clearCart() {
            // when
            cartService.clearCart(cart.getId());

            // then
            List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
            assertThat(cartItems.size()).isEqualTo(0);
        }
    }
}
