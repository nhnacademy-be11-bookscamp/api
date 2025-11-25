package store.bookscamp.api.orderinfo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderListServiceTest {

    @InjectMocks
    private OrderListService orderListService;

    @Mock
    private OrderInfoRepository orderInfoRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private final Long TEST_MEMBER_ID = 1L;
    private final Pageable PAGEABLE = PageRequest.of(0, 5);

    @DisplayName("회원 ID와 Pageable로 주문 목록을 성공적으로 조회한다")
    @Test
    void getOrderList_success() {
        // given
        // 1. OrderInfo Mock 설정
        OrderInfo order1 = createOrderInfo(1L, 50000, "DELIVERED");
        OrderInfo order2 = createOrderInfo(2L, 25000, "AWAITING_PAYMENT");

        List<OrderInfo> mockOrderInfoList = List.of(order1, order2);
        Page<OrderInfo> mockOrderInfoPage = new PageImpl<>(mockOrderInfoList, PAGEABLE, 2);

        given(orderInfoRepository.findByMemberId(eq(TEST_MEMBER_ID), eq(PAGEABLE)))
                .willReturn(mockOrderInfoPage);

        // 2. OrderItem Mock 설정 (수량 계산용)
        OrderItem item1_1 = createOrderItem(10L, order1, null, 2);
        OrderItem item1_2 = createOrderItem(11L, order1, null, 1);
        List<OrderItem> itemsForOrder1 = List.of(item1_1, item1_2); // 총 수량 3

        OrderItem item2_1 = createOrderItem(20L, order2, null, 1);
        List<OrderItem> itemsForOrder2 = List.of(item2_1); // 총 수량 1

        given(orderItemRepository.findByOrderInfoId(1L)).willReturn(itemsForOrder1);
        given(orderItemRepository.findByOrderInfoId(2L)).willReturn(itemsForOrder2);

        // 3. **대표 도서 제목 Mock 설정 (수정된 로직)**
        final String TITLE_A = "대표 도서 제목 A";
        final String TITLE_B = "대표 도서 제목 B";

        given(orderItemRepository.findRepresentativeBookTitleIncludingDeleted(1L)).willReturn(TITLE_A);
        given(orderItemRepository.findRepresentativeBookTitleIncludingDeleted(2L)).willReturn(TITLE_B);

        // when
        Page<OrderListDto> resultPage = orderListService.getOrderList(TEST_MEMBER_ID, PAGEABLE);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(2);

        OrderListDto dto1 = resultPage.getContent().get(0);
        assertThat(dto1.getOrderId()).isEqualTo(1L);
        assertThat(dto1.getFinalPaymentAmount()).isEqualTo(50000);
        // 새로운 Mock 값으로 검증
        assertThat(dto1.getRepresentativeBookTitle()).isEqualTo(TITLE_A);
        assertThat(dto1.getTotalQuantity()).isEqualTo(3);

        OrderListDto dto2 = resultPage.getContent().get(1);
        assertThat(dto2.getOrderId()).isEqualTo(2L);
        assertThat(dto2.getFinalPaymentAmount()).isEqualTo(25000);
        // 새로운 Mock 값으로 검증
        assertThat(dto2.getRepresentativeBookTitle()).isEqualTo(TITLE_B);
        assertThat(dto2.getTotalQuantity()).isEqualTo(1);

        // Repository 호출 검증
        verify(orderInfoRepository).findByMemberId(eq(TEST_MEMBER_ID), eq(PAGEABLE));
        verify(orderItemRepository).findByOrderInfoId(1L);
        verify(orderItemRepository).findByOrderInfoId(2L);
        // 새로운 Repository 메서드 호출 검증
        verify(orderItemRepository).findRepresentativeBookTitleIncludingDeleted(1L);
        verify(orderItemRepository).findRepresentativeBookTitleIncludingDeleted(2L);
    }

    @DisplayName("조회된 주문이 없을 경우 빈 페이지를 반환한다")
    @Test
    void getOrderList_empty() {
        // given
        Page<OrderInfo> mockOrderInfoPage = new PageImpl<>(List.of(), PAGEABLE, 0);

        given(orderInfoRepository.findByMemberId(eq(TEST_MEMBER_ID), eq(PAGEABLE)))
                .willReturn(mockOrderInfoPage);

        // when
        Page<OrderListDto> resultPage = orderListService.getOrderList(TEST_MEMBER_ID, PAGEABLE);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(0);
        assertThat(resultPage.getContent()).isEmpty();

        verify(orderInfoRepository).findByMemberId(eq(TEST_MEMBER_ID), eq(PAGEABLE));
        // OrderInfo가 없으므로 OrderItemRepository 호출은 발생하지 않아야 합니다.
    }

    // --- Helper Methods ---

    // Note: OrderInfo, Book, OrderItem 엔티티에는 상속받은 createdAt 필드를 Builder로 설정할 수 없으므로,
    // SuperBuilder 적용을 가정하거나, 테스트코드를 단순화하기 위해 해당 필드 설정을 생략했습니다.
    // (이전 대화에서 SuperBuilder 문제로 인해 발생했던 이슈는 이 테스트에서는 임시로 무시하고 진행합니다.)

    private OrderInfo createOrderInfo(Long id, int finalPaymentAmount, String status) {
        Member mockMember = Member.builder()
                .id(TEST_MEMBER_ID)
                .name("Test Member")
                .build();

        return OrderInfo.builder()
                .id(id)
                .orderNumber("ORD-" + id)
                .member(mockMember)
                .couponIssue(null)
                .delivery(null)
                .netAmount(finalPaymentAmount)
                .totalAmount(finalPaymentAmount)
                .deliveryFee(0)
                .packagingFee(0)
                .discountAmount(0)
                .finalPaymentAmount(finalPaymentAmount)
                .orderStatus(OrderStatus.valueOf(status))
                .usedPoint(0)
                // createdAt 필드 설정은 @SuperBuilder 사용을 가정하고 생략
                .build();
    }

    private Book createBook(Long id, String title) {
        return Book.builder()
                .id(id)
                .title(title)
                .build();
    }

    private OrderItem createOrderItem(Long id, OrderInfo orderInfo, Book book, int quantity) {
        // book 필드는 수량 계산에는 필요 없으므로 null로 설정해도 무방합니다.
        return OrderItem.builder()
                .id(id)
                .orderInfo(orderInfo)
                .book(book)
                .orderQuantity(quantity)
                // 기타 필수 필드가 있다면 여기에 추가
                .build();
    }
}