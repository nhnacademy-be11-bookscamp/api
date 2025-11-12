package store.bookscamp.api.orderinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.book.entity.BookStatus.SOLD_OUT;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_AVAILABLE;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.INSUFFICIENT_STOCK;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderItemRequestDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareRequestDto;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;

@SpringBootTest
@Transactional
class OrderPrepareServiceTest {

    @Autowired
    private OrderPrepareService orderPrepareService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    @Autowired
    private PackagingRepository packagingRepository;

    @Autowired
    private RankRepository rankRepository;

    private Book book1;
    private Book book2;
    private DeliveryPolicy deliveryPolicy;
    private Packaging packaging1;
    private Packaging packaging2;
    private Member member;

    @BeforeEach
    void setUp() {
        // 배송 정책 생성
        deliveryPolicy = deliveryPolicyRepository.save(
                new DeliveryPolicy(30000, 3000)
        );

        // 도서 생성
        book1 = bookRepository.save(new Book(
                "테스트 책 1",
                "책 설명 1",
                null,
                "출판사1",
                LocalDate.of(2024, 1, 1),
                "123456789001",
                "저자1",
                AVAILABLE,
                false,
                20000,
                18000,
                100,
                0L
        ));

        book2 = bookRepository.save(new Book(
                "테스트 책 2",
                "책 설명 2",
                null,
                "출판사2",
                LocalDate.of(2024, 2, 1),
                "123456789002",
                "저자2",
                AVAILABLE,
                false,
                15000,
                13000,
                50,
                0L
        ));

        // 포장지 생성
        packaging1 = packagingRepository.save(new Packaging("일반 포장", 500, "http://image.url/normal"));
        packaging2 = packagingRepository.save(new Packaging("고급 포장", 1000, "http://image.url/premium"));

        // 회원 생성
        Rank rank = rankRepository.save(new Rank(null, "BRONZE", 0, 100000));
        member = memberRepository.save(new Member(
                "테스트회원",
                "password123",
                "test@example.com",
                "01012345678",
                5000,
                rank,
                NORMAL,
                LocalDate.now(),
                "testuser",
                LocalDateTime.now(),
                LocalDate.of(1990, 1, 1)
        ));
    }

    @Nested
    @DisplayName("주문서 준비 시")
    class PrepareTest {

        @Test
        @DisplayName("정상적으로 주문서 준비 정보 반환 - 단일 도서")
        void prepare_singleBook_success() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 2);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, null);

            // then
            assertThat(result.orderItems()).hasSize(1);
            assertThat(result.orderItems().get(0).bookId()).isEqualTo(book1.getId());
            assertThat(result.orderItems().get(0).quantity()).isEqualTo(2);
            assertThat(result.orderItems().get(0).bookTotalAmount()).isEqualTo(36000); // 18000 * 2

            assertThat(result.priceInfo().netAmount()).isEqualTo(36000);
            assertThat(result.priceInfo().deliveryFee()).isEqualTo(0); // 30000원 이상 무료배송
            assertThat(result.priceInfo().totalAmount()).isEqualTo(36000);

            assertThat(result.availablePoint()).isEqualTo(0); // 비회원
            assertThat(result.availableCoupons()).isEmpty(); // 비회원
            assertThat(result.availablePackagings()).hasSize(2);
        }

        @Test
        @DisplayName("정상적으로 주문서 준비 정보 반환 - 여러 도서")
        void prepare_multipleBooks_success() {
            // given
            OrderItemRequestDto item1 = new OrderItemRequestDto(book1.getId(), 1);
            OrderItemRequestDto item2 = new OrderItemRequestDto(book2.getId(), 2);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(item1, item2));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, null);

            // then
            assertThat(result.orderItems()).hasSize(2);
            assertThat(result.priceInfo().netAmount()).isEqualTo(44000); // 18000 + 13000*2
            assertThat(result.priceInfo().deliveryFee()).isEqualTo(0); // 30000원 이상 무료배송
            assertThat(result.priceInfo().totalAmount()).isEqualTo(44000);
        }

        @Test
        @DisplayName("회원인 경우 포인트 정보 포함")
        void prepare_withMember_includesPoints() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 1);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, member.getId());

            // then
            assertThat(result.availablePoint()).isEqualTo(5000);
        }

        @Test
        @DisplayName("존재하지 않는 도서로 주문 준비 시 BOOK_NOT_FOUND 예외 발생")
        void prepare_bookNotFound_throwsException() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(9999L, 1);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // expect
            assertThatThrownBy(() -> orderPrepareService.prepare(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(BOOK_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("판매 불가능한 도서로 주문 준비 시 BOOK_NOT_AVAILABLE 예외 발생")
        void prepare_bookNotAvailable_throwsException() {
            // given
            Book unavailableBook = bookRepository.save(new Book(
                    "품절 책",
                    "설명",
                    null,
                    "출판사",
                    LocalDate.now(),
                    "123456789999",
                    "저자",
                    SOLD_OUT,
                    false,
                    10000,
                    9000,
                    0,
                    0L
            ));

            OrderItemRequestDto itemDto = new OrderItemRequestDto(unavailableBook.getId(), 1);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // expect
            assertThatThrownBy(() -> orderPrepareService.prepare(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(BOOK_NOT_AVAILABLE.getMessage());
        }

        @Test
        @DisplayName("재고보다 많은 수량 주문 시 INSUFFICIENT_STOCK 예외 발생")
        void prepare_insufficientStock_throwsException() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 200); // 재고 100개
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // expect
            assertThatThrownBy(() -> orderPrepareService.prepare(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(INSUFFICIENT_STOCK.getMessage());
        }

        @Test
        @DisplayName("무료 배송 기준 금액 이상 구매 시 배송비 0원")
        void prepare_freeDelivery_whenAboveThreshold() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 2); // 36000원
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, null);

            // then
            assertThat(result.priceInfo().netAmount()).isEqualTo(36000);
            assertThat(result.priceInfo().deliveryFee()).isEqualTo(0); // 30000원 이상 무료
            assertThat(result.priceInfo().freeDeliveryThreshold()).isEqualTo(30000);
        }

        @Test
        @DisplayName("무료 배송 기준 미만 구매 시 배송비 부과")
        void prepare_chargeDelivery_whenBelowThreshold() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 1); // 18000원
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, null);

            // then
            assertThat(result.priceInfo().netAmount()).isEqualTo(18000);
            assertThat(result.priceInfo().deliveryFee()).isEqualTo(3000);
            assertThat(result.priceInfo().totalAmount()).isEqualTo(21000);
        }

        @Test
        @DisplayName("포장 옵션 목록 조회")
        void prepare_returnsAvailablePackagings() {
            // given
            OrderItemRequestDto itemDto = new OrderItemRequestDto(book1.getId(), 1);
            OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of(itemDto));

            // when
            OrderPrepareDto result = orderPrepareService.prepare(request, null);

            // then
            assertThat(result.availablePackagings()).hasSize(2);
            assertThat(result.availablePackagings())
                    .extracting("name")
                    .containsExactlyInAnyOrder("일반 포장", "고급 포장");
        }
    }
}