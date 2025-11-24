package store.bookscamp.api.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store.bookscamp.api.common.config.FeignConfig;
import store.bookscamp.api.payment.adapter.dto.TossApprovalRequest;
import store.bookscamp.api.payment.adapter.dto.TossApprovalResponse;
import store.bookscamp.api.payment.adapter.dto.TossCancelResponse;

import java.util.Map;

@FeignClient(
        name = "tossPaymentClient",
        url = "${payment.toss.api-url}",
        configuration = FeignConfig.class
)
public interface TossPaymentClient {

    @PostMapping("/v1/payments/confirm")
    TossApprovalResponse approve(
            @RequestHeader("Authorization") String authorization,
            @RequestBody TossApprovalRequest request
    );

    @PostMapping("/v1/payments/{paymentKey}/cancel")
    TossCancelResponse cancel(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("paymentKey") String paymentKey,
            @RequestBody Map<String, String> request
    );
}