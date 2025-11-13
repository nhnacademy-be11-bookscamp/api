package store.bookscamp.api.orderinfo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.orderinfo.controller.request.DeliveryInfoRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderCreateRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderItemCreateRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderItemRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderPrepareRequest;
import store.bookscamp.api.orderinfo.service.OrderCreateService;
import store.bookscamp.api.orderinfo.service.OrderPrepareService;
import store.bookscamp.api.orderinfo.service.dto.CouponDto;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;
import store.bookscamp.api.orderinfo.service.dto.PackagingDto;
import store.bookscamp.api.orderinfo.service.dto.PriceDto;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderPrepareService orderPrepareService;

    @MockitoBean
    private OrderCreateService orderCreateService;

    @Nested
    @DisplayName("POST /orders/prepare")
    class PrepareOrderTest {

        @Test
        @DisplayName("주문서 준비 요청 성공")
        void prepare_success() throws Exception {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
            OrderPrepareRequest request = new OrderPrepareRequest(List.of(itemRequest));

            OrderItemDto itemDto = new OrderItemDto(
                    1L,
                    "테스트 책",
                    "http://image.url",
                    18000,
                    2,
                    36000,
                    true
            );
            PriceDto priceDto = new PriceDto(36000, 3000, 39000, 30000);
            PackagingDto packaging1 = new PackagingDto(1L, "일반 포장", 500);
            PackagingDto packaging2 = new PackagingDto(2L, "고급 포장", 1000);
            CouponDto coupon = new CouponDto(
                    1L,
                    1L,
                    "10% 할인 쿠폰",
                    "RATE",
                    10,
                    10000,
                    5000,
                    3900
            );

            OrderPrepareDto responseDto = new OrderPrepareDto(
                    List.of(itemDto),
                    priceDto,
                    5000,
                    List.of(packaging1, packaging2),
                    List.of(coupon)
            );

            given(orderPrepareService.prepare(any(), eq(1L)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/orders/prepare")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-ID", 1L)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderItems").isArray())
                    .andExpect(jsonPath("$.orderItems[0].bookId").value(1))
                    .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                    .andExpect(jsonPath("$.orderItems[0].bookTotalAmount").value(36000))
                    .andExpect(jsonPath("$.priceInfo.netAmount").value(36000))
                    .andExpect(jsonPath("$.priceInfo.deliveryFee").value(3000))
                    .andExpect(jsonPath("$.priceInfo.totalAmount").value(39000))
                    .andExpect(jsonPath("$.availablePoint").value(5000))
                    .andExpect(jsonPath("$.availablePackagings").isArray())
                    .andExpect(jsonPath("$.availablePackagings.length()").value(2))
                    .andExpect(jsonPath("$.availableCoupons").isArray())
                    .andExpect(jsonPath("$.availableCoupons.length()").value(1));
        }

        @Test
        @DisplayName("비회원 주문서 준비 요청 성공")
        void prepare_nonMember_success() throws Exception {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(1L, 1);
            OrderPrepareRequest request = new OrderPrepareRequest(List.of(itemRequest));

            OrderItemDto itemDto = new OrderItemDto(
                    1L,
                    "테스트 책",
                    "http://image.url",
                    18000,
                    1,
                    18000,
                    true
            );
            PriceDto priceDto = new PriceDto(18000, 3000, 21000, 30000);

            OrderPrepareDto responseDto = new OrderPrepareDto(
                    List.of(itemDto),
                    priceDto,
                    0,  // 비회원 포인트 0
                    List.of(),
                    List.of()  // 비회원 쿠폰 없음
            );

            given(orderPrepareService.prepare(any(), eq(null)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/orders/prepare")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availablePoint").value(0))
                    .andExpect(jsonPath("$.availableCoupons").isEmpty());
        }

        @Test
        @DisplayName("빈 주문 목록으로 요청 시 400 에러")
        void prepare_emptyItems_badRequest() throws Exception {
            // given
            OrderPrepareRequest request = new OrderPrepareRequest(List.of());

            // when & then
            mockMvc.perform(post("/orders/prepare")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /orders")
    class CreateOrderTest {

        @Test
        @DisplayName("회원 주문 생성 성공")
        void createOrder_member_success() throws Exception {
            // given
            OrderItemCreateRequest item = new OrderItemCreateRequest(1L, 2, 1L);
            DeliveryInfoRequest deliveryInfo = new DeliveryInfoRequest(
                    "수령인",
                    "010-1234-5678",
                    12345,
                    "서울시 강남구",
                    "101동 101호",
                    LocalDate.now().plusDays(3),
                    "문 앞에 놓아주세요"
            );
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(item),
                    deliveryInfo,
                    null,
                    0,
                    null
            );

            OrderCreateDto responseDto = new OrderCreateDto(100L, 39500);

            given(orderCreateService.createOrder(any(), eq(1L)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-ID", 1L)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(100))
                    .andExpect(jsonPath("$.finalAmount").value(39500));
        }

        @Test
        @DisplayName("포인트 사용하여 주문 생성 성공")
        void createOrder_withPoints_success() throws Exception {
            // given
            OrderItemCreateRequest item = new OrderItemCreateRequest(1L, 2, null);
            DeliveryInfoRequest deliveryInfo = new DeliveryInfoRequest(
                    "수령인",
                    "010-1234-5678",
                    12345,
                    "서울시 강남구",
                    "101동 101호",
                    LocalDate.now().plusDays(3),
                    null
            );
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(item),
                    deliveryInfo,
                    null,
                    5000,  // 포인트 사용
                    null
            );

            OrderCreateDto responseDto = new OrderCreateDto(100L, 34000);

            given(orderCreateService.createOrder(any(), eq(1L)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-ID", 1L)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.finalAmount").value(34000));
        }

        @Test
        @DisplayName("배송 정보 없이 요청 시 400 에러")
        void createOrder_noDeliveryInfo_badRequest() throws Exception {
            // given
            OrderItemCreateRequest item = new OrderItemCreateRequest(1L, 1, null);
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(item),
                    null,  // 배송 정보 없음
                    null,
                    0,
                    null
            );

            // when & then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-ID", 1L)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("음수 포인트 사용 시 400 에러")
        void createOrder_negativePoints_badRequest() throws Exception {
            // given
            OrderItemCreateRequest item = new OrderItemCreateRequest(1L, 1, null);
            DeliveryInfoRequest deliveryInfo = new DeliveryInfoRequest(
                    "수령인",
                    "010-1234-5678",
                    12345,
                    "서울시 강남구",
                    "101동 101호",
                    LocalDate.now().plusDays(3),
                    null
            );
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(item),
                    deliveryInfo,
                    null,
                    -1000,  // 음수 포인트
                    null
            );

            // when & then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-ID", 1L)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}