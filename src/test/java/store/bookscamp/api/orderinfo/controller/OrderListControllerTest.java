package store.bookscamp.api.orderinfo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.OrderListService;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;

@WebMvcTest(OrderListController.class)
class OrderListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderListService orderListService;

    @MockitoBean
    private OrderDetailService orderDetailService;

    @Nested
    @DisplayName("GET /orders/list")
    class GetMyOrdersTest {

        @Test
        @DisplayName("회원 주문 목록 조회 성공")
        void getMyOrders_member_success() throws Exception {
            // given
            Long memberId = 1L;

            OrderListDto dto = new OrderListDto(
                    100L,
                    LocalDateTime.of(2025, 1, 1, 10, 0),
                    OrderStatus.DELIVERED,
                    "테스트 책",
                    3,
                    15000
            );

            Page<OrderListDto> dtoPage =
                    new PageImpl<>(List.of(dto));

            given(orderListService.getOrderList(eq(memberId), any(Pageable.class)))
                    .willReturn(dtoPage);

            // when & then
            mockMvc.perform(get("/orders/list")
                            .header("X-User-ID", memberId)
                            .param("page", "0")
                            .param("size", "5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].orderId").value(100))
                    .andExpect(jsonPath("$.content[0].representativeBookTitle").value("테스트 책"))
                    .andExpect(jsonPath("$.content[0].totalQuantity").value(3))
                    .andExpect(jsonPath("$.content[0].finalPaymentAmount").value(15000));
        }

        @Test
        @DisplayName("주문이 없는 경우 빈 목록 반환")
        void getMyOrders_empty_success() throws Exception {
            // given
            Long memberId = 1L;
            Page<OrderListDto> emptyPage = new PageImpl<>(List.of());

            given(orderListService.getOrderList(eq(memberId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/orders/list")
                            .header("X-User-ID", memberId)
                            .param("page", "0")
                            .param("size", "5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /orders/{orderId}")
    class GetOrderDetailTest {

        @Test
        @DisplayName("memberId 없으면 401 반환")
        void getOrderDetail_unauthorized_whenNoMemberId() throws Exception {
            // given
            Long orderId = 1L;

            // when & then
            mockMvc.perform(get("/orders/{orderId}", orderId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("회원 주문 상세 조회 성공")
        void getOrderDetail_member_success() throws Exception {
            // given
            Long memberId = 1L;
            Long orderId = 100L;

            OrderDetailItemResponse item = new OrderDetailItemResponse(
                    1L,
                    "상세 조회 도서",
                    2,
                    10000,
                    20000
            );

            OrderDetailResponse detailResponse = new OrderDetailResponse(
                    orderId,
                    LocalDateTime.of(2025, 1, 2, 9, 0),
                    OrderStatus.DELIVERED.name(),
                    List.of(item),
                    "수령인",
                    "010-0000-0000",
                    "서울시 어딘가 123",
                    "문 앞에 놔주세요",
                    20000,
                    3000,
                    0,
                    0,
                    0,
                    23000
            );

            given(orderDetailService.getOrderDetail(eq(memberId), eq(orderId)))
                    .willReturn(detailResponse);

            // when & then
            mockMvc.perform(get("/orders/{orderId}", orderId)
                            .header("X-User-ID", memberId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.orderStatus").value("DELIVERED"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].bookId").value(1))
                    .andExpect(jsonPath("$.items[0].bookTitle").value("상세 조회 도서"))
                    .andExpect(jsonPath("$.recipientName").value("수령인"))
                    .andExpect(jsonPath("$.finalPaymentAmount").value(23000));
        }
    }
}
