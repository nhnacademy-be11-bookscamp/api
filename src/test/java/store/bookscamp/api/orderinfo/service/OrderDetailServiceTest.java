package store.bookscamp.api.orderinfo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.delivery.entity.Delivery;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceTest {

    @InjectMocks
    private OrderDetailService orderDetailService;

    @Mock
    private OrderInfoRepository orderInfoRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private final Long TEST_MEMBER_ID = 1L;
    private final Long OTHER_MEMBER_ID = 99L;
    private final Long TEST_ORDER_ID = 10L;

    // --- 주문 상세 조회 성공 테스트 ---

    @DisplayName("회원 ID와 주문 ID로 주문 상세 정보를 성공적으로 조회한다 (배송 정보 포함)")
    @Test
    void getOrderDetail_withDeliveryInfo_success() {
        // given
        // 1. 엔티티 Mock 생성 및 Builder 설정 (Helper 메서드 사용)
        Member mockMember = createMockMember(TEST_MEMBER_ID);
        Delivery mockDelivery = createMockDelivery();
        OrderInfo mockOrderInfo = createMockOrderInfo(TEST_ORDER_ID, mockMember, mockDelivery);

        Book book1 = createMockBook(100L, "Java Programming", 30000);
        Book book2 = createMockBook(101L, "Spring Guide", 20000);

        OrderItem item1 = createMockOrderItem(1L, mockOrderInfo, book1, 2, 30000); // 60000
        OrderItem item2 = createMockOrderItem(2L, mockOrderInfo, book2, 1, 20000); // 20000
        List<OrderItem> mockOrderItems = List.of(item1, item2); // 상품 총액 (netAmount) 80000원

        // 2. Repository Mocking
        given(orderInfoRepository.findById(eq(TEST_ORDER_ID)))
                .willReturn(Optional.of(mockOrderInfo));
        given(orderItemRepository.findByOrderInfoId(eq(TEST_ORDER_ID)))
                .willReturn(mockOrderItems);

        // when
        OrderDetailResponse result = orderDetailService.getOrderDetail(TEST_MEMBER_ID, TEST_ORDER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(TEST_ORDER_ID);
        // OrderStatus는 Enum.name()으로 String 변환되어야 함
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.DELIVERED.name());

        // 결제 정보 검증
        assertThat(result.productAmount()).isEqualTo(80000);
        assertThat(result.discountAmount()).isEqualTo(5000);
        assertThat(result.finalPaymentAmount()).isEqualTo(78000);

        // 배송 정보 검증
        assertThat(result.recipientName()).isEqualTo("김철수");
        // Delivery 주소 매핑 로직: roadNameAddress + " " + detailAddress
        assertThat(result.deliveryAddress()).isEqualTo("서울시 강남구 역삼로 123 201호");

        // 주문 항목 검증
        assertThat(result.items()).hasSize(2);

        OrderDetailItemResponse responseItem1 = result.items().get(0);
        assertThat(responseItem1.bookTitle()).isEqualTo("Java Programming");
        assertThat(responseItem1.orderQuantity()).isEqualTo(2);
        assertThat(responseItem1.bookTotalAmount()).isEqualTo(60000);

        // Repository 호출 검증
        verify(orderInfoRepository).findById(eq(TEST_ORDER_ID));
        verify(orderItemRepository).findByOrderInfoId(eq(TEST_ORDER_ID));
    }

    @DisplayName("배송 정보가 없는 주문의 상세 정보를 성공적으로 조회한다")
    @Test
    void getOrderDetail_withoutDeliveryInfo_success() {
        // given
        Member mockMember = createMockMember(TEST_MEMBER_ID);
        // Delivery가 null인 OrderInfo 생성
        OrderInfo mockOrderInfo = createMockOrderInfo(TEST_ORDER_ID, mockMember, null);

        // OrderItem은 필수
        Book book1 = createMockBook(100L, "No Delivery Book", 10000);
        OrderItem item1 = createMockOrderItem(1L, mockOrderInfo, book1, 1, 10000);
        List<OrderItem> mockOrderItems = List.of(item1);

        given(orderInfoRepository.findById(eq(TEST_ORDER_ID)))
                .willReturn(Optional.of(mockOrderInfo));
        given(orderItemRepository.findByOrderInfoId(eq(TEST_ORDER_ID)))
                .willReturn(mockOrderItems);

        // when
        OrderDetailResponse result = orderDetailService.getOrderDetail(TEST_MEMBER_ID, TEST_ORDER_ID);

        // then
        assertThat(result).isNotNull();
        // 레코드 필드 접근자 사용 (getRecipientName() -> recipientName())
        assertThat(result.recipientName()).isNull();
        assertThat(result.deliveryAddress()).isNull();

        // 다른 필드가 정상적으로 매핑되었는지 확인
        assertThat(result.productAmount()).isEqualTo(80000); // Mock OrderInfo의 netAmount 값
    }

    // --- 예외 (실패) 테스트 ---
    // (이 부분은 Response 구조와 무관하므로 이전 코드와 동일하게 유지됩니다.)

    @DisplayName("존재하지 않는 주문 ID로 조회 시 ORDER_NOT_FOUND 예외가 발생한다")
    @Test
    void getOrderDetail_orderNotFound() {
        // given
        given(orderInfoRepository.findById(eq(TEST_ORDER_ID)))
                .willReturn(Optional.empty());

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(TEST_MEMBER_ID, TEST_ORDER_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("주문자와 요청 memberId가 일치하지 않으면 ORDER_NOT_FOUND 예외가 발생한다 (권한 없음)")
    @Test
    void getOrderDetail_unauthorizedMember() {
        // given
        Member ownerMember = createMockMember(TEST_MEMBER_ID);
        OrderInfo mockOrderInfo = createMockOrderInfo(TEST_ORDER_ID, ownerMember, null);

        given(orderInfoRepository.findById(eq(TEST_ORDER_ID)))
                .willReturn(Optional.of(mockOrderInfo));

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(OTHER_MEMBER_ID, TEST_ORDER_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("memberId가 null일 경우 ORDER_NOT_FOUND 예외가 발생한다 (비회원 접근 차단)")
    @Test
    void getOrderDetail_nullMemberId() {
        // given
        Member ownerMember = createMockMember(TEST_MEMBER_ID);
        OrderInfo mockOrderInfo = createMockOrderInfo(TEST_ORDER_ID, ownerMember, null);

        given(orderInfoRepository.findById(eq(TEST_ORDER_ID)))
                .willReturn(Optional.of(mockOrderInfo));

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(null, TEST_ORDER_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    // --- Helper Methods ---
    // (엔티티 Mocking을 위해 @Builder를 사용하며, 이는 Response 레코드와는 무관합니다.)

    private Member createMockMember(Long id) {
        return Member.builder().id(id).name("Test Member " + id).build();
    }

    private Delivery createMockDelivery() {
        return Delivery.builder()
                .recipientName("김철수")
                .recipientPhone("010-1234-5678")
                .roadNameAddress("서울시 강남구 역삼로 123")
                .detailAddress("201호")
                .deliveryMemo("문앞ㄱㄱ.")
                .build();
    }

    private OrderInfo createMockOrderInfo(Long id, Member member, Delivery delivery) {
        return OrderInfo.builder()
                .id(id)
                .orderNumber("ORD-" + id)
                .member(member)
                .couponIssue(null)
                .delivery(delivery)
                .netAmount(80000)
                .totalAmount(83000)
                .deliveryFee(3000)
                .packagingFee(0)
                .discountAmount(5000)
                .finalPaymentAmount(78000)
                .orderStatus(OrderStatus.DELIVERED)
                .usedPoint(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Book createMockBook(Long id, String title, int price) {
        return Book.builder().id(id).title(title).regularPrice(price).build();
    }

    private OrderItem createMockOrderItem(Long id, OrderInfo orderInfo, Book book, int quantity, int bookPrice) {
        return OrderItem.builder()
                .id(id)
                .orderInfo(orderInfo)
                .book(book)
                .orderQuantity(quantity)
                .bookPrice(bookPrice)
                .bookTotalAmount(quantity * bookPrice)
                .build();
    }
}