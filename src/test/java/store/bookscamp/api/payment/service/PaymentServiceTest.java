package store.bookscamp.api.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.cart.service.CartService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.OrderCartMappingService;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.payment.adapter.PaymentAdapter;
import store.bookscamp.api.payment.adapter.PaymentApprovalResponse;
import store.bookscamp.api.payment.entity.Payment;
import store.bookscamp.api.payment.entity.PaymentMethod;
import store.bookscamp.api.payment.repository.PaymentRepository;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.RewardType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import store.bookscamp.api.rank.entity.Rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    PaymentService paymentService;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    OrderInfoRepository orderInfoRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    PointHistoryRepository pointHistoryRepository;
    @Mock
    PaymentAdapter paymentAdapter;
    @Mock
    OrderCartMappingService orderCartMappingService;
    @Mock
    CartService cartService;

    private OrderInfo mockOrderInfo;
    private Member mockMember;
    private Book mockBook;
    private OrderItem mockOrderItem;

    @BeforeEach
    void setUp() {
        mockOrderInfo = mock(OrderInfo.class);
        mockMember = mock(Member.class);
        mockBook = mock(Book.class);
        mockOrderItem = mock(OrderItem.class);

        lenient().when(mockOrderItem.getBook()).thenReturn(mockBook);
        lenient().when(mockOrderItem.getOrderQuantity()).thenReturn(1);
        lenient().when(orderItemRepository.findByOrderInfoId(anyLong())).thenReturn(List.of(mockOrderItem));
        lenient().when(orderCartMappingService.getAndDeleteMapping(anyString())).thenReturn(1L);
    }

    @Test
    @DisplayName("결제 성공 시 주문상태 변경, 재고 감소, 장바구니 삭제, 회원 혜택 처리가 수행되어야 한다")
    void confirmPayment_Success_Toss() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.AWAITING_PAYMENT);
        given(mockOrderInfo.getFinalPaymentAmount()).willReturn(10000);
        given(paymentRepository.existsByOrderInfo(mockOrderInfo)).willReturn(false);

        PaymentApprovalResponse response = new PaymentApprovalResponse("key", "num", 10000, "CARD", LocalDateTime.now());
        given(paymentAdapter.approve(anyString(), anyString(), anyInt())).willReturn(response);

        given(mockOrderInfo.getMember()).willReturn(mockMember);
        mockMemberBenefitSetup(100, 5000, RewardType.RATE, 10);

        paymentService.confirmPayment("key", "num", 10000);

        verify(paymentAdapter, times(1)).approve(anyString(), anyString(), anyInt());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(mockBook, times(1)).decreaseStock(1);
        verify(mockMember, times(1)).usePoint(anyInt());
        verify(mockMember, times(1)).earnPoint(anyInt());
        verify(pointHistoryRepository, times(2)).save(any());
        verify(mockOrderInfo, times(1)).changeOrderStatus(OrderStatus.PENDING);
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("결제 금액이 0원(포인트 전액)이면 외부 결제 승인 없이 내부 결제 처리되어야 한다")
    void confirmPayment_Success_PointOnly() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.AWAITING_PAYMENT);
        given(mockOrderInfo.getFinalPaymentAmount()).willReturn(0);
        given(paymentRepository.existsByOrderInfo(mockOrderInfo)).willReturn(false);
        given(mockOrderInfo.getMember()).willReturn(mockMember);

        Payment payment = paymentService.confirmPayment("key", "num", 0);

        verify(paymentAdapter, never()).approve(anyString(), anyString(), anyInt());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
    }

    @Test
    @DisplayName("주문 정보를 찾을 수 없으면 ORDER_NOT_FOUND 예외 발생")
    void confirmPayment_Exception_OrderNotFound() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment("key", "num", 1000))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("주문 상태가 AWAITING_PAYMENT가 아니면 ORDER_NOT_AWAITING_PAYMENT 예외 발생")
    void confirmPayment_Exception_OrderStatus() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.DELIVERED);

        assertThatThrownBy(() -> paymentService.confirmPayment("key", "num", 1000))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("이미 결제 정보가 존재하면 ORDER_ALREADY_PAID 예외 발생")
    void confirmPayment_Exception_AlreadyPaid() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.AWAITING_PAYMENT);
        given(paymentRepository.existsByOrderInfo(mockOrderInfo)).willReturn(true);

        assertThatThrownBy(() -> paymentService.confirmPayment("key", "num", 1000))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("요청 금액과 주문 금액이 일치하지 않으면 PAYMENT_AMOUNT_MISMATCH 예외 발생")
    void confirmPayment_Exception_AmountMismatch() {
        given(orderInfoRepository.findByOrderNumber(anyString())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.AWAITING_PAYMENT);
        given(mockOrderInfo.getFinalPaymentAmount()).willReturn(5000);

        assertThatThrownBy(() -> paymentService.confirmPayment("key", "num", 1000))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Toss 결제 취소 시 재고 복구, 혜택 복구, 결제 정보 삭제가 수행되어야 한다")
    void cancelPayment_Success_TossPayment() {
        Payment mockPayment = mock(Payment.class);
        given(orderInfoRepository.findById(anyLong())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.PENDING);
        given(paymentRepository.findByOrderInfo(mockOrderInfo)).willReturn(Optional.of(mockPayment));
        given(mockPayment.getPaymentKey()).willReturn("toss_key");

        given(mockOrderInfo.getMember()).willReturn(mockMember);
        mockMemberBenefitSetup(1000, 10000, RewardType.AMOUNT, 500);

        paymentService.cancelPayment(1L, "단순 변심");

        verify(paymentAdapter, times(1)).cancel("toss_key", "단순 변심");
        verify(mockBook, times(1)).increaseStock(1);
        verify(mockMember, times(1)).earnPoint(1000);
        verify(mockMember, times(1)).usePoint(500);
        verify(mockOrderInfo, times(1)).changeOrderStatus(OrderStatus.CANCELLED);
        verify(paymentRepository, times(1)).delete(mockPayment);
    }

    @Test
    @DisplayName("포인트 결제 취소 시 외부 결제 취소 호출 없이 내부 복구 로직만 수행되어야 한다")
    void cancelPayment_Success_PointPayment_NonMember() {
        Payment mockPayment = mock(Payment.class);
        given(orderInfoRepository.findById(anyLong())).willReturn(Optional.of(mockOrderInfo));
        given(mockOrderInfo.getOrderStatus()).willReturn(OrderStatus.PENDING);
        given(paymentRepository.findByOrderInfo(mockOrderInfo)).willReturn(Optional.of(mockPayment));
        given(mockPayment.getPaymentKey()).willReturn(null);

        given(mockOrderInfo.getMember()).willReturn(null);

        paymentService.cancelPayment(1L, "단순 변심");

        verify(paymentAdapter, never()).cancel(anyString(), anyString());
        verify(mockBook, times(1)).increaseStock(1);
        verify(mockOrderInfo, times(1)).changeOrderStatus(OrderStatus.CANCELLED);
        verify(paymentRepository, times(1)).delete(mockPayment);
    }

    private void mockMemberBenefitSetup(int usedPoint, int netAmount, RewardType rewardType, int rewardValue) {
        lenient().when(mockOrderInfo.getUsedPoint()).thenReturn(usedPoint);
        lenient().when(mockOrderInfo.getNetAmount()).thenReturn(netAmount);
        lenient().when(mockOrderInfo.getCouponIssue()).thenReturn(mock(CouponIssue.class));

        Rank mockRank = mock(Rank.class);
        PointPolicy mockPolicy = mock(PointPolicy.class);

        lenient().when(mockMember.getRank()).thenReturn(mockRank);
        lenient().when(mockRank.getPointPolicy()).thenReturn(mockPolicy);
        lenient().when(mockPolicy.getRewardType()).thenReturn(rewardType);
        lenient().when(mockPolicy.getRewardValue()).thenReturn(rewardValue);
    }
}