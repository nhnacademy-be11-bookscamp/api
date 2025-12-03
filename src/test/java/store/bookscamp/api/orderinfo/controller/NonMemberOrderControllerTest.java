package store.bookscamp.api.orderinfo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.orderinfo.controller.request.NonMemberInfoRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;

@WebMvcTest(NonMemberOrderController.class)
class NonMemberOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderDetailService orderDetailService;

    private final String baseUrl = "/orders/non-member";
    private final String testOrderNumber = "NONMEMBER-20251202-001";
    private final String validPassword = "1234";

    @Nested
    @DisplayName("POST /orders/non-member/{orderNumber}")
    class GetNonMemberOrderDetailTest {

        @Test
        @DisplayName("유효한 주문번호와 비밀번호로 비회원 주문 상세 조회를 성공한다")
        void getNonMemberOrderList_success() throws Exception {
            // given
            NonMemberInfoRequest request = new NonMemberInfoRequest(validPassword);

            OrderDetailResponse mockResponse = new OrderDetailResponse(
                    10L,
                    LocalDateTime.of(2025, 12, 1, 10, 0),
                    "ORDER_COMPLETED",
                    List.of(new OrderDetailItemResponse(200L, "비회원 도서", 1, 25000, 25000)),
                    "김비회",
                    "010-9999-8888",
                    "서울시 마포구",
                    "경비실",
                    25000,
                    3000,
                    0,
                    0,
                    0,
                    28000
            );

            given(orderDetailService.getNonMemberOrderDetail(
                    eq(testOrderNumber),
                    any(NonMemberInfoDto.class)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post(baseUrl + "/{orderNumber}", testOrderNumber)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(10L))
                    .andExpect(jsonPath("$.finalPaymentAmount").value(28000));
        }

        @Test
        @DisplayName("비밀번호 불일치 또는 주문번호가 없을 경우 404 NOT_FOUND를 반환한다")
        void getNonMemberOrderList_fail_invalidCredentials() throws Exception {

            NonMemberInfoRequest request = new NonMemberInfoRequest(validPassword);

            given(orderDetailService.getNonMemberOrderDetail(
                    eq(testOrderNumber),
                    any(NonMemberInfoDto.class)))
                    .willThrow(new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

            mockMvc.perform(post(baseUrl + "/{orderNumber}", testOrderNumber)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string(ErrorCode.ORDER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("비밀번호가 3자리 이하로 짧을 경우 400 BAD_REQUEST를 반환한다 (@Valid 실패)")
        void getNonMemberOrderList_fail_passwordTooShort() throws Exception {
            // given
            NonMemberInfoRequest request = new NonMemberInfoRequest("123");

            // when & then
            mockMvc.perform(post(baseUrl + "/{orderNumber}", testOrderNumber)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 응답 본문 검증을 생략하고 상태 코드만 확인
        }

        @Test
        @DisplayName("비밀번호가 9자리 이상으로 길 경우 400 BAD_REQUEST를 반환한다 (@Valid 실패)")
        void getNonMemberOrderList_fail_passwordTooLong() throws Exception {
            // given
            NonMemberInfoRequest request = new NonMemberInfoRequest("123456789");

            mockMvc.perform(post(baseUrl + "/{orderNumber}", testOrderNumber)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 필드가 누락되어 요청된 경우 400 BAD_REQUEST를 반환한다 (@Valid NotBlank 실패)")
        void getNonMemberOrderList_fail_passwordMissing() throws Exception {
            NonMemberInfoRequest request = new NonMemberInfoRequest(null);

            mockMvc.perform(post(baseUrl + "/{orderNumber}", testOrderNumber)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}