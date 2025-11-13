package store.bookscamp.api.orderinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_NOT_ALLOWED_FOR_NON_MEMBER;
import static store.bookscamp.api.common.exception.ErrorCode.INSUFFICIENT_POINT;
import static store.bookscamp.api.common.exception.ErrorCode.NON_MEMBER_INFO_REQUIRED;
import static store.bookscamp.api.common.exception.ErrorCode.POINT_NOT_ALLOWED_FOR_NON_MEMBER;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.PENDING;

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
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.nonmember.entity.NonMember;
import store.bookscamp.api.nonmember.repository.NonMemberRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.DeliveryInfoDto;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderRequestDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.RewardType;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;

@SpringBootTest
@Transactional
class OrderCreateServiceTest {

    @Autowired
    private OrderCreateService orderCreateService;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    @Autowired
    private PackagingRepository packagingRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private NonMemberRepository nonMemberRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    private Book book1;
    private Book book2;
    private DeliveryPolicy deliveryPolicy;
    private Packaging packaging;
    private Member member;
    private DeliveryInfoDto deliveryInfo;

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
        packaging = packagingRepository.save(new Packaging("일반 포장", 500, "http://image.url/normal"));

        // 포인트 정책 및 등급 생성
        PointPolicy pointPolicy = pointPolicyRepository.save(
                new PointPolicy(store.bookscamp.api.pointpolicy.entity.PointPolicyType.STANDARD, RewardType.RATE, 5)
        );
        Rank rank = rankRepository.save(new Rank(pointPolicy, "BRONZE", 0, 100000));

        // 회원 생성
        member = memberRepository.save(new Member(
                "테스트회원",
                "password123",
                "test@example.com",
                "01012345678",
                10000,
                rank,
                NORMAL,
                LocalDate.now(),
                "testuser",
                LocalDateTime.now(),
                LocalDate.of(1990, 1, 1)
        ));

        // 배송 정보
        deliveryInfo = new DeliveryInfoDto(
                "수령인",
                "01098765432",
                12345,
                "서울시 강남구",
                "101동 101호",
                LocalDate.now().plusDays(3),
                "문 앞에 놓아주세요"
        );
    }

    @Nested
    @DisplayName("회원 주문 생성 시")
    class CreateMemberOrderTest {

        @Test
        @DisplayName("정상적으로 주문 생성")
        void createOrder_member_success() {
            // given
            OrderItemCreateDto item1 = new OrderItemCreateDto(book1.getId(), 2, packaging.getId());
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item1),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            int initialStock = book1.getStock();

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, member.getId());

            // then
            assertThat(result.orderId()).isNotNull();
            assertThat(result.finalPaymentAmount()).isEqualTo(36500); // 36000 + 500(포장비)

            // 주문 정보 확인
            OrderInfo orderInfo = orderInfoRepository.findById(result.orderId()).orElseThrow();
            assertThat(orderInfo.getMember().getId()).isEqualTo(member.getId());
            assertThat(orderInfo.getNetAmount()).isEqualTo(36000);
            assertThat(orderInfo.getDeliveryFee()).isEqualTo(0);
            assertThat(orderInfo.getPackagingFee()).isEqualTo(500);

            // 주문 아이템 확인
            List<OrderItem> orderItems = orderItemRepository.findAll();
            assertThat(orderItems).hasSize(1);
            assertThat(orderItems.get(0).getBook().getId()).isEqualTo(book1.getId());
            assertThat(orderItems.get(0).getOrderQuantity()).isEqualTo(2);

            // 재고 차감 확인
            Book updatedBook = bookRepository.findById(book1.getId()).orElseThrow();
            assertThat(updatedBook.getStock()).isEqualTo(initialStock - 2);
        }

        @Test
        @DisplayName("포인트 사용하여 주문 생성")
        void createOrder_withPoints_success() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 2, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    5000,  // 5000 포인트 사용
                    null
            );

            int initialPoint = member.getPoint();

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, member.getId());

            // then
            assertThat(result.finalPaymentAmount()).isEqualTo(31000); // 36000 - 5000

            // 회원 포인트 확인 (사용 -5000, 적립 +1800)
            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
            int earnedPoint = (int) Math.floor(36000 * 0.05); // 1800
            assertThat(updatedMember.getPoint()).isEqualTo(initialPoint - 5000 + earnedPoint);

            // 포인트 사용 내역 확인
            List<PointHistory> useHistories = pointHistoryRepository.findAll().stream()
                    .filter(h -> h.getPointType() == PointType.USE)
                    .toList();
            assertThat(useHistories).hasSize(1);
            assertThat(useHistories.get(0).getPointAmount()).isEqualTo(5000);

            // 포인트 적립 내역 확인
            List<PointHistory> earnHistories = pointHistoryRepository.findAll().stream()
                    .filter(h -> h.getPointType() == PointType.EARN)
                    .toList();
            assertThat(earnHistories).hasSize(1);
            assertThat(earnHistories.get(0).getPointAmount()).isEqualTo(earnedPoint);
        }

        @Test
        @DisplayName("쿠폰 사용하여 주문 생성")
        void createOrder_withCoupon_success() {
            // given
            Coupon coupon = couponRepository.save(new Coupon(
                    TargetType.WELCOME,
                    null,
                    DiscountType.AMOUNT,
                    3000,
                    10000,
                    null,
                    30,
                    "테스트 쿠폰"
            ));
            CouponIssue couponIssue = couponIssueRepository.save(
                    new CouponIssue(coupon, member, LocalDateTime.now().plusDays(30))
            );

            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 2, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    couponIssue.getId(),
                    0,
                    null
            );

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, member.getId());

            // then
            assertThat(result.finalPaymentAmount()).isEqualTo(33000); // 36000 - 3000(쿠폰)

            // 쿠폰 사용 확인
            CouponIssue updatedCoupon = couponIssueRepository.findById(couponIssue.getId()).orElseThrow();
            assertThat(updatedCoupon.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("포인트 적립 확인")
        void createOrder_earnPoints_success() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 2, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            int initialPoint = member.getPoint();
            int expectedEarnPoint = (int) Math.floor(36000 * 5 / 100.0); // 5% 적립

            // when
            orderCreateService.createOrder(request, member.getId());

            // then
            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(updatedMember.getPoint()).isEqualTo(initialPoint + expectedEarnPoint);

            // 포인트 적립 내역 확인
            List<PointHistory> earnHistories = pointHistoryRepository.findAll().stream()
                    .filter(h -> h.getPointType() == PointType.EARN)
                    .toList();
            assertThat(earnHistories).hasSize(1);
            assertThat(earnHistories.get(0).getPointAmount()).isEqualTo(expectedEarnPoint);
        }

        @Test
        @DisplayName("보유 포인트보다 많은 포인트 사용 시 INSUFFICIENT_POINT 예외 발생")
        void createOrder_insufficientPoints_throwsException() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 1, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    20000,  // 보유: 10000
                    null
            );

            // expect
            assertThatThrownBy(() -> orderCreateService.createOrder(request, member.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(INSUFFICIENT_POINT.getMessage());
        }
    }

    @Nested
    @DisplayName("비회원 주문 생성 시")
    class CreateNonMemberOrderTest {

        @Test
        @DisplayName("정상적으로 비회원 주문 생성")
        void createOrder_nonMember_success() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 1, null);
            NonMemberInfoDto nonMemberInfo = new NonMemberInfoDto("password123");
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    nonMemberInfo
            );

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, null);

            // then
            assertThat(result.orderId()).isNotNull();

            // NonMember 엔티티 생성 확인
            OrderInfo orderInfo = orderInfoRepository.findById(result.orderId()).orElseThrow();
            List<NonMember> nonMembers = nonMemberRepository.findAll();
            assertThat(nonMembers).hasSize(1);
            assertThat(nonMembers.get(0).getOrderInfo().getId()).isEqualTo(orderInfo.getId());
        }

        @Test
        @DisplayName("비회원 정보 없이 주문 시 NON_MEMBER_INFO_REQUIRED 예외 발생")
        void createOrder_nonMemberWithoutInfo_throwsException() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 1, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    null  // 비회원 정보 없음
            );

            // expect
            assertThatThrownBy(() -> orderCreateService.createOrder(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(NON_MEMBER_INFO_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("비회원이 포인트 사용 시 POINT_NOT_ALLOWED_FOR_NON_MEMBER 예외 발생")
        void createOrder_nonMemberWithPoints_throwsException() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 1, null);
            NonMemberInfoDto nonMemberInfo = new NonMemberInfoDto("password123");
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    1000,  // 비회원이 포인트 사용
                    nonMemberInfo
            );

            // expect
            assertThatThrownBy(() -> orderCreateService.createOrder(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(POINT_NOT_ALLOWED_FOR_NON_MEMBER.getMessage());
        }

        @Test
        @DisplayName("비회원이 쿠폰 사용 시 COUPON_NOT_ALLOWED_FOR_NON_MEMBER 예외 발생")
        void createOrder_nonMemberWithCoupon_throwsException() {
            // given
            Coupon coupon = couponRepository.save(new Coupon(
                    TargetType.WELCOME,
                    null,
                    DiscountType.AMOUNT,
                    3000,
                    10000,
                    null,
                    30,
                    ""
            ));

            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 1, null);
            NonMemberInfoDto nonMemberInfo = new NonMemberInfoDto("password123");
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    coupon.getId(),  // 비회원이 쿠폰 사용
                    0,
                    nonMemberInfo
            );

            // expect
            assertThatThrownBy(() -> orderCreateService.createOrder(request, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(COUPON_NOT_ALLOWED_FOR_NON_MEMBER.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 금액 계산 테스트")
    class CalculationTest {

        @Test
        @DisplayName("여러 도서 주문 시 금액 계산 정확성")
        void createOrder_multipleBooks_correctCalculation() {
            // given
            OrderItemCreateDto item1 = new OrderItemCreateDto(book1.getId(), 2, packaging.getId());
            OrderItemCreateDto item2 = new OrderItemCreateDto(book2.getId(), 1, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item1, item2),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, member.getId());

            // then
            // 36000(book1*2) + 13000(book2*1) + 0(배송비, 무료배송) + 500(포장비)
            assertThat(result.finalPaymentAmount()).isEqualTo(49500);
        }

        @Test
        @DisplayName("무료 배송 기준 이상 구매 시 배송비 0원")
        void createOrder_freeDelivery_success() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(book1.getId(), 2, null); // 36000원
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            // when
            OrderCreateDto result = orderCreateService.createOrder(request, member.getId());

            // then
            OrderInfo orderInfo = orderInfoRepository.findById(result.orderId()).orElseThrow();
            assertThat(orderInfo.getDeliveryFee()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 도서로 주문 시 BOOK_NOT_FOUND 예외 발생")
        void createOrder_bookNotFound_throwsException() {
            // given
            OrderItemCreateDto item = new OrderItemCreateDto(9999L, 1, null);
            OrderRequestDto request = new OrderRequestDto(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            // expect
            assertThatThrownBy(() -> orderCreateService.createOrder(request, member.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(BOOK_NOT_FOUND.getMessage());
        }
    }
}