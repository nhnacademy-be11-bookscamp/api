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

    private final Long testMemberId = 1L;
    private final Pageable pageable = PageRequest.of(0, 5);

    @DisplayName("회원 ID와 Pageable로 주문 목록을 성공적으로 조회한다")
    @Test
    void getOrderList_success() {

        OrderInfo order1 = createOrderInfo(1L, 50000, "DELIVERED");
        OrderInfo order2 = createOrderInfo(2L, 25000, "AWAITING_PAYMENT");

        List<OrderInfo> mockOrderInfoList = List.of(order1, order2);
        Page<OrderInfo> mockOrderInfoPage = new PageImpl<>(mockOrderInfoList, pageable, 2);

        given(orderInfoRepository.findByMemberId(testMemberId, pageable))
                .willReturn(mockOrderInfoPage);

        OrderItem item11 = createOrderItem(10L, order1, null, 2);
        OrderItem item12 = createOrderItem(11L, order1, null, 1);
        List<OrderItem> itemsForOrder1 = List.of(item11, item12); // 총 수량 3

        OrderItem item21 = createOrderItem(20L, order2, null, 1);
        List<OrderItem> itemsForOrder2 = List.of(item21); // 총 수량 1

        given(orderItemRepository.findByOrderInfoId(1L)).willReturn(itemsForOrder1);
        given(orderItemRepository.findByOrderInfoId(2L)).willReturn(itemsForOrder2);

        final String TITLE_A = "대표 도서 제목 A";
        final String TITLE_B = "대표 도서 제목 B";

        given(orderItemRepository.findRepresentativeBookTitleIncludingDeleted(1L)).willReturn(TITLE_A);
        given(orderItemRepository.findRepresentativeBookTitleIncludingDeleted(2L)).willReturn(TITLE_B);

        // when
        Page<OrderListDto> resultPage = orderListService.getOrderList(testMemberId, pageable);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(2);

        OrderListDto dto1 = resultPage.getContent().get(0);
        assertThat(dto1.getOrderId()).isEqualTo(1L);
        assertThat(dto1.getFinalPaymentAmount()).isEqualTo(50000);

        assertThat(dto1.getRepresentativeBookTitle()).isEqualTo(TITLE_A);
        assertThat(dto1.getTotalQuantity()).isEqualTo(3);

        OrderListDto dto2 = resultPage.getContent().get(1);
        assertThat(dto2.getOrderId()).isEqualTo(2L);
        assertThat(dto2.getFinalPaymentAmount()).isEqualTo(25000);

        assertThat(dto2.getRepresentativeBookTitle()).isEqualTo(TITLE_B);
        assertThat(dto2.getTotalQuantity()).isEqualTo(1);

        verify(orderInfoRepository).findByMemberId(testMemberId, pageable);
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
        Page<OrderInfo> mockOrderInfoPage = new PageImpl<>(List.of(), pageable, 0);

        given(orderInfoRepository.findByMemberId(testMemberId, pageable))
                .willReturn(mockOrderInfoPage);

        // when
        Page<OrderListDto> resultPage = orderListService.getOrderList(testMemberId, pageable);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(0);
        assertThat(resultPage.getContent()).isEmpty();

        verify(orderInfoRepository).findByMemberId(testMemberId, pageable);
    }
    private OrderInfo createOrderInfo(Long id, int finalPaymentAmount, String status) {
        Member mockMember = Member.builder()
                .id(testMemberId)
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
                .build();
    }

    private OrderItem createOrderItem(Long id, OrderInfo orderInfo, Book book, int quantity) {
        return OrderItem.builder()
                .id(id)
                .orderInfo(orderInfo)
                .book(book)
                .orderQuantity(quantity)
                .build();
    }
}