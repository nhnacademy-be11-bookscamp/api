package store.bookscamp.api.payment.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.payment.adapter.dto.TossApprovalRequest;
import store.bookscamp.api.payment.adapter.dto.TossApprovalResponse;
import store.bookscamp.api.payment.adapter.dto.TossCancelResponse;
import store.bookscamp.api.payment.config.TossPaymentProperties;
import store.bookscamp.api.payment.feign.TossPaymentClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TossPaymentAdapterTest {

    @InjectMocks
    TossPaymentAdapter adapter;
    @Mock
    TossPaymentClient tossPaymentClient;
    @Mock
    TossPaymentProperties properties;

    private final String SECRET_KEY = "test_sk";
    private final String ENCODED_AUTH_HEADER = "Basic dGVzdF9zazo=";

    @Test
    @DisplayName("approve: Toss 승인 요청 성공 시 내부 DTO로 정확히 변환되어야 한다")
    void approve_Success_ReturnsApprovalResponse() {
        given(properties.secretKey()).willReturn(SECRET_KEY);

        TossApprovalResponse tossResponse = new TossApprovalResponse(
                "pay_key", "order_id", 10000, "카드", "2025-12-04T10:30:00+09:00"
        );

        given(tossPaymentClient.approve(contains(ENCODED_AUTH_HEADER), any(TossApprovalRequest.class))).willReturn(tossResponse);

        PaymentApprovalResponse result = adapter.approve("pay_key", "order_id", 10000);

        assertThat(result.paymentKey()).isEqualTo("pay_key");
        assertThat(result.approvedAt()).isEqualTo(LocalDateTime.of(2025, 12, 4, 10, 30, 0));
        verify(tossPaymentClient).approve(anyString(), any());
    }

    @Test
    @DisplayName("approve: Feign 통신 실패 시 PAYMENT_APPROVAL_FAILED 예외 발생")
    void approve_Exception_ThrowsApplicationException() {
        given(properties.secretKey()).willReturn(SECRET_KEY);

        doThrow(new RuntimeException()).when(tossPaymentClient).approve(anyString(), any(TossApprovalRequest.class));

        assertThatThrownBy(() -> adapter.approve("key", "id", 10000))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("cancel: 취소 응답에 취소 정보가 포함되어 있으면 해당 날짜로 반환되어야 한다")
    void cancel_Success_WithCancelInfo() {
        given(properties.secretKey()).willReturn(SECRET_KEY);

        TossCancelResponse.Cancel tossCancel = new TossCancelResponse.Cancel("2025-12-04T11:00:00+09:00");
        TossCancelResponse tossResponse = new TossCancelResponse(
                "pay_key", "order_id", List.of(tossCancel)
        );

        given(tossPaymentClient.cancel(contains(ENCODED_AUTH_HEADER), anyString(), any())).willReturn(tossResponse);

        PaymentCancelResponse result = adapter.cancel("pay_key", "고객 요청");

        assertThat(result.paymentKey()).isEqualTo("pay_key");
        assertThat(result.canceledAt()).isEqualTo(LocalDateTime.of(2025, 12, 4, 11, 0, 0));
    }

    @Test
    @DisplayName("cancel: 취소 응답에 취소 정보가 없으면 현재 시간으로 반환되어야 한다 (100% 커버리지 목적)")
    void cancel_Success_WithoutCancelInfo_UsesNow() {
        given(properties.secretKey()).willReturn(SECRET_KEY);

        TossCancelResponse tossResponse = new TossCancelResponse(
                "pay_key", "order_id", Collections.emptyList()
        );

        given(tossPaymentClient.cancel(contains(ENCODED_AUTH_HEADER), anyString(), any())).willReturn(tossResponse);

        PaymentCancelResponse result = adapter.cancel("pay_key", "고객 요청");

        assertThat(result.canceledAt()).isNotNull();
        assertThat(result.canceledAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("cancel: Feign 통신 실패 시 PAYMENT_CANCEL_FAILED 예외 발생")
    void cancel_Exception_ThrowsApplicationException() {
        given(properties.secretKey()).willReturn(SECRET_KEY);

        doThrow(new RuntimeException()).when(tossPaymentClient).cancel(anyString(), anyString(), any());

        assertThatThrownBy(() -> adapter.cancel("key", "사유"))
                .isInstanceOf(ApplicationException.class);
    }
}