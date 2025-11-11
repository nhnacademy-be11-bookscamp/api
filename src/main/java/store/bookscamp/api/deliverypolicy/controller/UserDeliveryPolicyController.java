package store.bookscamp.api.deliverypolicy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.deliverypolicy.controller.DeliveryPolicyController.DeliveryFeeResponse;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.service.DeliveryPolicyService;

@RestController
@Tag(name = "배송비정책 API", description = "DeliveryPolicy API입니다.")
@RequestMapping("/delivery-policies")  // 사용자 전용 경로
@RequiredArgsConstructor
public class UserDeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;

    @GetMapping("/current")
    @Operation(summary = "현재 배송비 정책 조회")
    // @RequiredRole("USER")
    public ResponseEntity<DeliveryPolicyGetResponse> getCurrent() {
        return ResponseEntity.ok(deliveryPolicyService.getCurrent());
    }

    @GetMapping("/is-free")
    @Operation(summary = "주문총액으로 무료배송 여부 판단", description = "query parameter: orderTotal(원)")
    // @RequiredRole("USER")
    public ResponseEntity<Boolean> isFree(@RequestParam int orderTotal) {
        return ResponseEntity.ok(deliveryPolicyService.isFreeByTotal(orderTotal));
    }

    @GetMapping("/fee")
    @Operation(summary = "주문총액으로 배송비 계산", description = "query parameter: orderTotal(원)")
    // @RequiredRole("USER")
    public ResponseEntity<DeliveryFeeResponse> fee(@RequestParam int orderTotal) {
        int fee = deliveryPolicyService.calculateFee(orderTotal);
        return ResponseEntity.ok(new DeliveryFeeResponse(orderTotal, fee, fee == 0));
    }
}
