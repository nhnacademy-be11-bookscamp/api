package store.bookscamp.api.orderinfo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import store.bookscamp.api.nonmember.entity.NonMember;
import store.bookscamp.api.nonmember.repository.NonMemberRepository;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private NonMemberRepository nonMemberRepository;

    private final Long testMemberId = 1L;
    private final Long otherMemberId = 99L;
    private final Long testOrderId = 10L;

    private final String testOrderNumber = "ORD-NM-12345";
    private final String validPassword = "abcd";

    @Nested
    @DisplayName("getNonMemberOrderDetail")
    class NonMemberOrderDetailTest {

        @DisplayName("유효한 주문번호로 비회원 주문 상세 조회를 성공한다")
        @Test
        void getNonMemberOrderDetail_success() {
            NonMemberInfoDto nonMemberInfoDto = new NonMemberInfoDto(validPassword);

            Delivery mockDelivery = createMockDelivery();
            OrderInfo mockOrderInfo = createMockOrderInfo(testOrderId, null, mockDelivery); // 비회원 주문 (member=null)

            Book book1 = createMockBook(100L, "비회원 도서", 30000);
            OrderItem item1 = createMockOrderItem(1L, mockOrderInfo, book1, 2, 30000);
            List<OrderItem> mockOrderItems = List.of(item1);

            NonMember mockNonMember = new NonMember(mockOrderInfo, validPassword);

            given(nonMemberRepository.findByOrderInfo_OrderNumber(testOrderNumber))
                    .willReturn(Optional.of(mockNonMember));

            given(orderItemRepository.findByOrderInfoId(testOrderId))
                    .willReturn(mockOrderItems);

            OrderDetailResponse result = orderDetailService.getNonMemberOrderDetail(testOrderNumber, nonMemberInfoDto);

            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(testOrderId);
            assertThat(result.productAmount()).isEqualTo(80000);
            assertThat(result.recipientName()).isEqualTo("김철수");

            verify(nonMemberRepository).findByOrderInfo_OrderNumber(testOrderNumber);
            verify(orderItemRepository).findByOrderInfoId(testOrderId);
        }

        @DisplayName("주문번호를 찾을 수 없으면 ORDER_NOT_FOUND 예외가 발생한다")
        @Test
        void getNonMemberOrderDetail_orderNotFound() {
            NonMemberInfoDto nonMemberInfoDto = new NonMemberInfoDto(validPassword);

            given(nonMemberRepository.findByOrderInfo_OrderNumber(testOrderNumber))
                    .willReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                orderDetailService.getNonMemberOrderDetail(testOrderNumber, nonMemberInfoDto);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
            verify(nonMemberRepository).findByOrderInfo_OrderNumber(testOrderNumber);
        }
    }


    @DisplayName("회원 ID와 주문 ID로 주문 상세 정보를 성공적으로 조회한다 (배송 정보 포함)")
    @Test
    void getOrderDetail_withDeliveryInfo_success() {
        Member mockMember = createMockMember(testMemberId);
        Delivery mockDelivery = createMockDelivery();
        OrderInfo mockOrderInfo = createMockOrderInfo(testOrderId, mockMember, mockDelivery);

        Book book1 = createMockBook(100L, "Java Programming", 30000);
        Book book2 = createMockBook(101L, "Spring Guide", 20000);

        OrderItem item1 = createMockOrderItem(1L, mockOrderInfo, book1, 2, 30000); // 60000
        OrderItem item2 = createMockOrderItem(2L, mockOrderInfo, book2, 1, 20000); // 20000
        List<OrderItem> mockOrderItems = List.of(item1, item2); // 상품 총액 (netAmount) 80000원

        given(orderInfoRepository.findById(testOrderId))
                .willReturn(Optional.of(mockOrderInfo));
        given(orderItemRepository.findByOrderInfoId(testOrderId))
                .willReturn(mockOrderItems);

        OrderDetailResponse result = orderDetailService.getOrderDetail(testMemberId, testOrderId);

        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(testOrderId);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.DELIVERED.name());

        assertThat(result.productAmount()).isEqualTo(80000);
        assertThat(result.discountAmount()).isEqualTo(5000);
        assertThat(result.finalPaymentAmount()).isEqualTo(78000);

        assertThat(result.recipientName()).isEqualTo("김철수");
        assertThat(result.deliveryAddress()).isEqualTo("서울시 강남구 역삼로 123 201호");

        assertThat(result.items()).hasSize(2);

        OrderDetailItemResponse responseItem1 = result.items().get(0);
        assertThat(responseItem1.bookTitle()).isEqualTo("Java Programming");
        assertThat(responseItem1.orderQuantity()).isEqualTo(2);
        assertThat(responseItem1.bookTotalAmount()).isEqualTo(60000);

        verify(orderInfoRepository).findById(testOrderId);
        verify(orderItemRepository).findByOrderInfoId(testOrderId);
    }

    @DisplayName("배송 정보가 없는 주문의 상세 정보를 성공적으로 조회한다")
    @Test
    void getOrderDetail_withoutDeliveryInfo_success() {
        Member mockMember = createMockMember(testMemberId);
        OrderInfo mockOrderInfo = createMockOrderInfo(testOrderId, mockMember, null);

        Book book1 = createMockBook(100L, "No Delivery Book", 10000);
        OrderItem item1 = createMockOrderItem(1L, mockOrderInfo, book1, 1, 10000);
        List<OrderItem> mockOrderItems = List.of(item1);

        given(orderInfoRepository.findById(testOrderId))
                .willReturn(Optional.of(mockOrderInfo));
        given(orderItemRepository.findByOrderInfoId(testOrderId))
                .willReturn(mockOrderItems);

        OrderDetailResponse result = orderDetailService.getOrderDetail(testMemberId, testOrderId);

        assertThat(result).isNotNull();
        assertThat(result.recipientName()).isNull();
        assertThat(result.deliveryAddress()).isNull();

        assertThat(result.productAmount()).isEqualTo(80000); // Mock OrderInfo의 netAmount 값
    }

    @DisplayName("존재하지 않는 주문 ID로 조회 시 ORDER_NOT_FOUND 예외가 발생한다")
    @Test
    void getOrderDetail_orderNotFound() {
        given(orderInfoRepository.findById(testOrderId))
                .willReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(testMemberId, testOrderId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("주문자와 요청 memberId가 일치하지 않으면 ORDER_NOT_FOUND 예외가 발생한다 (권한 없음)")
    @Test
    void getOrderDetail_unauthorizedMember() {
        Member ownerMember = createMockMember(testMemberId);
        OrderInfo mockOrderInfo = createMockOrderInfo(testOrderId, ownerMember, null);

        given(orderInfoRepository.findById(testOrderId))
                .willReturn(Optional.of(mockOrderInfo));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(otherMemberId, testOrderId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("memberId가 null일 경우 ORDER_NOT_FOUND 예외가 발생한다 (비회원 접근 차단)")
    @Test
    void getOrderDetail_nullMemberId() {
        Member ownerMember = createMockMember(testMemberId);
        OrderInfo mockOrderInfo = createMockOrderInfo(testOrderId, ownerMember, null);

        given(orderInfoRepository.findById(testOrderId))
                .willReturn(Optional.of(mockOrderInfo));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderDetailService.getOrderDetail(null, testOrderId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

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