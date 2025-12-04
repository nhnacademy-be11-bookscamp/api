package store.bookscamp.api.orderinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.common.exception.ErrorCode.*;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.delivery.entity.Delivery;
import store.bookscamp.api.delivery.repository.DeliveryRepository;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderStatusUpdateDto;

@SpringBootTest
@Transactional
@DisplayName("주문 상태 관리 서비스 테스트")
class OrderStatusServiceTest {

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    private DeliveryPolicy deliveryPolicy;

    @BeforeEach
    void setUp() {
        deliveryPolicy = new DeliveryPolicy(5000, 30000);
        deliveryPolicyRepository.save(deliveryPolicy);
    }

    @Nested
    @DisplayName("관리자 주문 상태 변경")
    class UpdateOrderStatus {

        @Test
        @DisplayName("PENDING에서 SHIPPING으로 변경 성공")
        void updateOrderStatus_PendingToShipping_Success() {
            // given
            OrderInfo orderInfo = createOrderInfo(PENDING);
            orderInfoRepository.save(orderInfo);

            // when
            OrderStatusUpdateDto result = orderStatusService.updateOrderStatus(orderInfo.getId(), SHIPPING);

            // then
            assertThat(result.orderStatus()).isEqualTo(SHIPPING);
            assertThat(result.orderId()).isEqualTo(orderInfo.getId());
            assertThat(result.orderNumber()).isEqualTo(orderInfo.getOrderNumber());
        }

        @Test
        @DisplayName("SHIPPING에서 DELIVERED로 변경 성공")
        void updateOrderStatus_ShippingToDelivered_Success() {
            // given
            OrderInfo orderInfo = createOrderInfo(SHIPPING);
            orderInfoRepository.save(orderInfo);

            // when
            OrderStatusUpdateDto result = orderStatusService.updateOrderStatus(orderInfo.getId(), DELIVERED);

            // then
            assertThat(result.orderStatus()).isEqualTo(DELIVERED);
        }

        @Test
        @DisplayName("이미 같은 상태로 변경 시도 시 예외 발생")
        void updateOrderStatus_SameStatus_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(PENDING);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), PENDING))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ORDER_STATUS_ALREADY_SET.getMessage());
        }

        @Test
        @DisplayName("취소된 주문 상태 변경 시도 시 예외 발생")
        void updateOrderStatus_CancelledOrder_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(CANCELLED);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), SHIPPING))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(CANCELLED_ORDER_CANNOT_BE_UPDATED.getMessage());
        }

        @Test
        @DisplayName("반품된 주문 상태 변경 시도 시 예외 발생")
        void updateOrderStatus_ReturnedOrder_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(RETURNED);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), PENDING))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(RETURNED_ORDER_CANNOT_BE_UPDATED.getMessage());
        }

        @Test
        @DisplayName("결제 대기 중인 주문은 관리자가 변경할 수 없음")
        void updateOrderStatus_AwaitingPaymentOrder_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(AWAITING_PAYMENT);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), PENDING))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(AWAITING_PAYMENT_ORDER_CANNOT_BE_UPDATED.getMessage());
        }

        @Test
        @DisplayName("PENDING에서 DELIVERED로 직접 변경 불가")
        void updateOrderStatus_PendingToDelivered_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(PENDING);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), DELIVERED))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(INVALID_ORDER_STATUS_TRANSITION.getMessage());
        }

        @Test
        @DisplayName("배송 완료된 주문은 반품만 가능")
        void updateOrderStatus_DeliveredOrder_OnlyReturnAllowed() {
            // given
            OrderInfo orderInfo = createOrderInfo(DELIVERED);
            orderInfoRepository.save(orderInfo);

            // when & then - 반품은 성공
            OrderStatusUpdateDto result = orderStatusService.updateOrderStatus(orderInfo.getId(), RETURNED);
            assertThat(result.orderStatus()).isEqualTo(RETURNED);
        }

        @Test
        @DisplayName("배송 완료된 주문을 PENDING으로 변경 시도 시 예외 발생")
        void updateOrderStatus_DeliveredToPending_ThrowsException() {
            // given
            OrderInfo orderInfo = createOrderInfo(DELIVERED);
            orderInfoRepository.save(orderInfo);

            // when & then
            assertThatThrownBy(() -> orderStatusService.updateOrderStatus(orderInfo.getId(), PENDING))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(DELIVERED_ORDER_CAN_ONLY_BE_RETURNED.getMessage());
        }
    }

    @Nested
    @DisplayName("배송 완료 자동 처리")
    class AutoCompleteShippingOrders {

        @Test
        @DisplayName("출고일이 어제인 배송 중 주문은 자동으로 완료 처리됨")
        void autoCompleteShippingOrders_Yesterday_Success() {
            // given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            OrderInfo orderInfo = createOrderInfoWithShippingDate(SHIPPING, yesterday);
            orderInfoRepository.save(orderInfo);

            // when
            int completedCount = orderStatusService.autoCompleteShippingOrders();

            // then
            assertThat(completedCount).isEqualTo(1);
            OrderInfo updated = orderInfoRepository.findById(orderInfo.getId()).orElseThrow();
            assertThat(updated.getOrderStatus()).isEqualTo(DELIVERED);
        }

        @Test
        @DisplayName("출고일이 오늘인 배송 중 주문은 자동 완료 처리되지 않음")
        void autoCompleteShippingOrders_Today_NotCompleted() {
            // given
            LocalDate today = LocalDate.now();
            OrderInfo orderInfo = createOrderInfoWithShippingDate(SHIPPING, today);
            orderInfoRepository.save(orderInfo);

            // when
            int completedCount = orderStatusService.autoCompleteShippingOrders();

            // then
            assertThat(completedCount).isEqualTo(0);
            OrderInfo unchanged = orderInfoRepository.findById(orderInfo.getId()).orElseThrow();
            assertThat(unchanged.getOrderStatus()).isEqualTo(SHIPPING);
        }

        @Test
        @DisplayName("여러 배송 중 주문 중 조건에 맞는 주문만 완료 처리됨")
        void autoCompleteShippingOrders_MultipleOrders_OnlyEligibleCompleted() {
            // given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();
            LocalDate twoDaysAgo = LocalDate.now().minusDays(2);

            OrderInfo order1 = createOrderInfoWithShippingDate(SHIPPING, yesterday);
            OrderInfo order2 = createOrderInfoWithShippingDate(SHIPPING, today);
            OrderInfo order3 = createOrderInfoWithShippingDate(SHIPPING, twoDaysAgo);
            OrderInfo order4 = createOrderInfoWithShippingDate(PENDING, yesterday); // 배송중 아님

            orderInfoRepository.saveAll(List.of(order1, order2, order3, order4));

            // when
            int completedCount = orderStatusService.autoCompleteShippingOrders();

            // then
            assertThat(completedCount).isEqualTo(2); // order1, order3만 완료

            assertThat(orderInfoRepository.findById(order1.getId()).orElseThrow().getOrderStatus())
                    .isEqualTo(DELIVERED);
            assertThat(orderInfoRepository.findById(order2.getId()).orElseThrow().getOrderStatus())
                    .isEqualTo(SHIPPING);
            assertThat(orderInfoRepository.findById(order3.getId()).orElseThrow().getOrderStatus())
                    .isEqualTo(DELIVERED);
            assertThat(orderInfoRepository.findById(order4.getId()).orElseThrow().getOrderStatus())
                    .isEqualTo(PENDING);
        }
    }

    // 테스트 헬퍼 메서드
    private OrderInfo createOrderInfo(OrderStatus status) {
        return createOrderInfoWithShippingDate(status, LocalDate.now());
    }

    private OrderInfo createOrderInfoWithShippingDate(OrderStatus status, LocalDate shippingDate) {
        Delivery delivery = new Delivery(
                deliveryPolicy,
                shippingDate,
                null,
                "홍길동",
                "01012345678",
                12345,
                "서울시 강남구",
                "101호",
                "문 앞에 놔주세요"
        );
        deliveryRepository.save(delivery);

        return new OrderInfo(
                "ORDER-TEST-" + System.nanoTime() + "-" + Math.random(),
                null, // member
                null, // couponIssue
                delivery,
                10000,  // netAmount
                15000,  // totalAmount
                5000,   // deliveryFee
                0,      // packagingFee
                0,      // discountAmount
                15000,  // finalPaymentAmount
                status,
                0       // usedPoint
        );
    }
}