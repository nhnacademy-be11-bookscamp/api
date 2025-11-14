package store.bookscamp.api.cart.query;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.cart.service.dto.CartItemDto;
import store.bookscamp.api.common.config.JpaConfig;

@Import(JpaConfig.class)
@DataJpaTest
class CartItemSearchQueryTest {

    @Autowired private EntityManager em;

    @Autowired private BookRepository bookRepository;
    @Autowired private BookImageRepository bookImageRepository;

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        // Book 1
        book1 = bookRepository.save(new Book(
                "도서1",
                "설명1",
                null,
                "출판사1",
                LocalDate.of(2000, 1, 1),
                "1111111111",
                "저자1",
                BookStatus.AVAILABLE,
                false,
                10000,
                9000,
                10,
                0L
        ));

        bookImageRepository.save(
                new BookImage(book1, "thumb1.jpg", true)
        );

        // Book 2
        book2 = bookRepository.save(new Book(
                "도서2",
                "설명2",
                null,
                "출판사2",
                LocalDate.of(2010, 5, 10),
                "2222222222",
                "저자2",
                BookStatus.AVAILABLE,
                false,
                20000,
                15000,
                7,
                0L
        ));

        bookImageRepository.save(
                new BookImage(book2, "thumb2.jpg", true)
        );
    }

    @Test
    @DisplayName("단건: cartItemId 기반 CartItemDto 조회 성공")
    void searchCartItemById_success() {
        // given
        CartItem cartItem = cartItemRepository.save(new CartItem(null, book1, 3));
        CartItemSearchQuery query = new CartItemSearchQuery(em);

        // when
        CartItemDto dto = query.searchCartItemById(cartItem.getId());

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getCartItemId()).isEqualTo(cartItem.getId());
        assertThat(dto.getBookId()).isEqualTo(book1.getId());
        assertThat(dto.getThumbnailImageUrl()).isEqualTo("thumb1.jpg");
        assertThat(dto.getQuantity()).isEqualTo(3);
        assertThat(dto.getRegularPrice()).isEqualTo(10000);
        assertThat(dto.getSalePrice()).isEqualTo(9000);
        assertThat(dto.getTotalPrice()).isEqualTo(3 * 9000);
    }

    @Test
    @DisplayName("다건: cartId 기반 CartItemDto 리스트 조회 성공")
    void searchCartItemsByCartId_success() {
        // given
        Cart cart = cartRepository.save(new Cart(null));

        CartItem ci1 = cartItemRepository.save(new CartItem(cart, book1, 2));
        CartItem ci2 = cartItemRepository.save(new CartItem(cart, book2, 5));

        CartItemSearchQuery query = new CartItemSearchQuery(em);

        // when
        List<CartItemDto> result = query.searchCartItemsByCartId(cart.getId());

        // then
        assertThat(result).hasSize(2);

        CartItemDto dto1 = result.stream()
                .filter(dto -> dto.getCartItemId().equals(ci1.getId()))
                .findFirst().orElseThrow();

        CartItemDto dto2 = result.stream()
                .filter(dto -> dto.getCartItemId().equals(ci2.getId()))
                .findFirst().orElseThrow();

        // dto1 검증
        assertThat(dto1.getBookId()).isEqualTo(book1.getId());
        assertThat(dto1.getThumbnailImageUrl()).isEqualTo("thumb1.jpg");
        assertThat(dto1.getQuantity()).isEqualTo(2);
        assertThat(dto1.getTotalPrice()).isEqualTo(2 * book1.getSalePrice());

        // dto2 검증
        assertThat(dto2.getBookId()).isEqualTo(book2.getId());
        assertThat(dto2.getThumbnailImageUrl()).isEqualTo("thumb2.jpg");
        assertThat(dto2.getQuantity()).isEqualTo(5);
        assertThat(dto2.getTotalPrice()).isEqualTo(5 * book2.getSalePrice());
    }
}
