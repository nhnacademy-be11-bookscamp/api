package store.bookscamp.api.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.DiscountType.RATE;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.coupon.entity.TargetType.CATEGORY;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.coupon.service.dto.CouponCreateDto;

@SpringBootTest
@Transactional
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("BOOK 타겟 쿠폰 생성 성공")
    void createCoupon_forBook_success() {
        // given
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
        CouponCreateDto dto = new CouponCreateDto(
                BOOK,
                book.getId(),
                RATE,
                10,
                5000,
                3000,
                30
        );

        // when
        Long couponId = couponService.createCoupon(dto);

        // then
        Coupon saved = couponRepository.findById(couponId).orElseThrow();
        assertThat(saved.getTargetType()).isEqualTo(BOOK);
        assertThat(saved.getTargetId()).isEqualTo(book.getId());
        assertThat(saved.getDiscountType()).isEqualTo(RATE);
        assertThat(saved.getDiscountValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("CATEGORY 타겟 쿠폰 생성 성공")
    void createCoupon_forCategory_success() {
        // given
        Category category = categoryRepository.save(new Category(null, "소설"));
        CouponCreateDto dto = new CouponCreateDto(
                CATEGORY,
                category.getId(),
                AMOUNT,
                2000,
                10000,
                null,
                15
        );

        // when
        Long couponId = couponService.createCoupon(dto);

        // then
        Coupon saved = couponRepository.findById(couponId).orElseThrow();
        assertThat(saved.getTargetType()).isEqualTo(CATEGORY);
        assertThat(saved.getDiscountType()).isEqualTo(AMOUNT);
        assertThat(saved.getDiscountValue()).isEqualTo(2000);
    }

    @Test
    @DisplayName("BOOK이 존재하지 않으면 BOOK_NOT_FOUND 예외 발생")
    void createCoupon_bookNotFound_fail() {
        // given
        CouponCreateDto dto = new CouponCreateDto(
                BOOK,
                999L,
                RATE,
                15,
                5000,
                3000,
                10
        );

        // expect
        assertThatThrownBy(() -> couponService.createCoupon(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(BOOK_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("CATEGORY가 존재하지 않으면 CATEGORY_NOT_FOUND 예외 발생")
    void createCoupon_categoryNotFound_fail() {
        // given
        CouponCreateDto dto = new CouponCreateDto(
                CATEGORY,
                999L,
                AMOUNT,
                1000,
                5000,
                null,
                10
        );

        // expect
        assertThatThrownBy(() -> couponService.createCoupon(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("쿠폰 목록을 조회할 수 있다")
    void listCoupons_success() {
        // given
        Coupon c1 = new Coupon(BOOK, 1L, RATE, 10, 5000, 3000, 7);
        Coupon c2 = new Coupon(CATEGORY, 2L, AMOUNT, 2000, 10000, null, 14);
        couponRepository.saveAll(List.of(c1, c2));

        // when
        List<Coupon> list = couponService.listCoupons();

        // then
        assertThat(list).hasSize(2);
        assertThat(list)
                .extracting(Coupon::getDiscountValue)
                .containsExactlyInAnyOrder(10, 2000);
    }

    @Test
    @DisplayName("쿠폰을 삭제하면 DB에서 제거된다")
    void deleteCoupon_success() {
        // given
        Coupon coupon = couponRepository.save(
                new Coupon(BOOK, 1L, RATE, 10, 5000, 3000, 7)
        );

        // when
        couponService.deleteCoupon(coupon.getId());

        // then
        assertThat(couponRepository.findById(coupon.getId())).isEmpty();
    }
}
