package store.bookscamp.api.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.payment.controller.request.PaymentCancelRequest;
import store.bookscamp.api.payment.controller.request.PaymentConfirmRequest;
import store.bookscamp.api.payment.entity.Payment;
import store.bookscamp.api.payment.service.PaymentService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /payments/confirm : 결제 승인 요청 성공 시 200 OK와 결제 정보를 반환해야 한다")
    void confirmPayment_Success() throws Exception {
        PaymentConfirmRequest request = new PaymentConfirmRequest("pay_key", "ORD-123", 15000);

        Payment mockPayment = mock(Payment.class);
        OrderInfo mockOrderInfo = mock(OrderInfo.class);

        given(paymentService.confirmPayment(anyString(), anyString(), anyInt())).willReturn(mockPayment);
        given(mockPayment.getId()).willReturn(5L);
        given(mockPayment.getOrderInfo()).willReturn(mockOrderInfo);
        given(mockOrderInfo.getId()).willReturn(10L);
        given(mockPayment.getPaidAmount()).willReturn(15000);
        given(mockPayment.getPaidAt()).willReturn(LocalDateTime.of(2025, 12, 4, 10, 0));

        ResultActions result = mockMvc.perform(
                post("/payments/confirm")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(5L))
                .andExpect(jsonPath("$.orderId").value(10L))
                .andExpect(jsonPath("$.paidAmount").value(15000))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/confirm : orderNumber가 공백이면 400 Bad Request가 발생해야 한다 (@NotBlank)")
    void confirmPayment_ValidationFailure_NotBlank() throws Exception {
        PaymentConfirmRequest invalidRequest = new PaymentConfirmRequest("pay_key", "", 15000);

        mockMvc.perform(
                        post("/payments/confirm")
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/confirm : amount가 null이면 400 Bad Request가 발생해야 한다 (@NotNull)")
    void confirmPayment_ValidationFailure_NotNull() throws Exception {
        // null을 JSON에 넣기 위해 수동으로 JSON 생성
        String jsonContent = "{\"paymentKey\": \"pay_key\", \"orderNumber\": \"ORD-123\", \"amount\": null}";

        mockMvc.perform(
                        post("/payments/confirm")
                                .content(jsonContent)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/confirm : amount가 0원(포인트 전액 결제)이면 성공해야 한다 (@PositiveOrZero)")
    void confirmPayment_Success_ZeroAmount() throws Exception {
        PaymentConfirmRequest request = new PaymentConfirmRequest(null, "ORD-123", 0);

        Payment mockPayment = mock(Payment.class);
        OrderInfo mockOrderInfo = mock(OrderInfo.class);

        given(paymentService.confirmPayment(null, "ORD-123", 0)).willReturn(mockPayment);
        given(mockPayment.getId()).willReturn(5L);
        given(mockPayment.getOrderInfo()).willReturn(mockOrderInfo);
        given(mockOrderInfo.getId()).willReturn(10L);
        given(mockPayment.getPaidAmount()).willReturn(0);
        given(mockPayment.getPaidAt()).willReturn(null);

        ResultActions result = mockMvc.perform(
                post("/payments/confirm")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(5L))
                .andExpect(jsonPath("$.orderId").value(10L))
                .andExpect(jsonPath("$.paidAmount").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/confirm : paymentKey가 null이어도 성공해야 한다 (0원 결제 시)")
    void confirmPayment_Success_NullPaymentKey() throws Exception {
        String jsonContent = "{\"paymentKey\": null, \"orderNumber\": \"ORD-123\", \"amount\": 0}";

        Payment mockPayment = mock(Payment.class);
        OrderInfo mockOrderInfo = mock(OrderInfo.class);

        given(paymentService.confirmPayment(null, "ORD-123", 0)).willReturn(mockPayment);
        given(mockPayment.getId()).willReturn(5L);
        given(mockPayment.getOrderInfo()).willReturn(mockOrderInfo);
        given(mockOrderInfo.getId()).willReturn(10L);
        given(mockPayment.getPaidAmount()).willReturn(0);
        given(mockPayment.getPaidAt()).willReturn(null);

        mockMvc.perform(
                        post("/payments/confirm")
                                .content(jsonContent)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(5L))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/confirm : amount가 음수이면 400 Bad Request가 발생해야 한다 (@PositiveOrZero)")
    void confirmPayment_ValidationFailure_NegativeAmount() throws Exception {
        PaymentConfirmRequest request = new PaymentConfirmRequest("pay_key", "ORD-123", -1000);

        mockMvc.perform(
                        post("/payments/confirm")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/cancel : 결제 취소 요청 성공 시 200 OK와 빈 응답을 반환해야 한다")
    void cancelPayment_Success() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest(10L, "고객 단순 변심");

        mockMvc.perform(
                        post("/payments/cancel")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/cancel : cancelReason이 공백이면 400 Bad Request가 발생해야 한다 (@NotBlank)")
    void cancelPayment_ValidationFailure_NotBlank() throws Exception {
        PaymentCancelRequest invalidRequest = new PaymentCancelRequest(10L, "");

        mockMvc.perform(
                        post("/payments/cancel")
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /payments/cancel : orderId가 0이하면 400 Bad Request가 발생해야 한다 (@Positive)")
    void cancelPayment_ValidationFailure_Positive() throws Exception {
        PaymentCancelRequest invalidRequest = new PaymentCancelRequest(0L, "테스트");

        mockMvc.perform(
                        post("/payments/cancel")
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}