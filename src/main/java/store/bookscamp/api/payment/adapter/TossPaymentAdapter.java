package store.bookscamp.api.payment.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.payment.adapter.dto.TossApprovalRequest;
import store.bookscamp.api.payment.adapter.dto.TossApprovalResponse;
import store.bookscamp.api.payment.adapter.dto.TossCancelResponse;
import store.bookscamp.api.payment.config.TossPaymentProperties;
import store.bookscamp.api.payment.feign.TossPaymentClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentAdapter implements PaymentAdapter {

    private final TossPaymentClient tossPaymentClient;
    private final TossPaymentProperties tossPaymentProperties;

    @Override
    public PaymentApprovalResponse approve(String paymentKey, String orderId, int amount) {
        log.info("[TOSS-ADAPTER] Approve START - paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        String authorization = "Basic " + encodeSecretKey();
        TossApprovalRequest request = new TossApprovalRequest(paymentKey, orderId, amount);

        try {
            log.debug("[TOSS-ADAPTER] Calling Toss API...");
            TossApprovalResponse response = tossPaymentClient.approve(authorization, request);
            log.info("[TOSS-ADAPTER] Toss API response received: {}", response);

            PaymentApprovalResponse result = new PaymentApprovalResponse(
                    response.paymentKey(),
                    response.orderId(),
                    response.totalAmount(),
                    response.method(),
                    OffsetDateTime.parse(response.approvedAt()).toLocalDateTime()
            );
            log.info("[TOSS-ADAPTER] Approve SUCCESS - paymentKey={}", result.paymentKey());
            return result;
        } catch (Exception e) {
            log.error("[TOSS-ADAPTER] Approve FAILED - paymentKey={}, orderId={}, error: {}", paymentKey, orderId, e.getMessage(), e);
            throw new ApplicationException(ErrorCode.PAYMENT_APPROVAL_FAILED, e);
        }
    }

    @Override
    public PaymentCancelResponse cancel(String paymentKey, String cancelReason) {
        String authorization = "Basic " + encodeSecretKey();
        Map<String, String> request = Map.of("cancelReason", cancelReason);

        try {
            TossCancelResponse response = tossPaymentClient.cancel(authorization, paymentKey, request);

            return new PaymentCancelResponse(
                    response.paymentKey(),
                    response.orderId(),
                    cancelReason,
                    OffsetDateTime.parse(response.canceledAt()).toLocalDateTime()
            );
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    private String encodeSecretKey() {
        return Base64.getEncoder()
                .encodeToString((tossPaymentProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
    }
}