package store.bookscamp.api.deliverypolicy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyCreateRequest;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.service.DeliveryPolicyService;

@RestController
@Tag(name = "배송비정책 API", description = "DeliveryPolicy API입니다.")
@RequestMapping("/delivery-policies")
@RequiredArgsConstructor
public class DeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;

    @PostMapping
    @Operation(summary = "배송비 정책 생성(관리자)")
    //@RequiredRole("ADMIN")
    public ResponseEntity<DeliveryPolicyGetResponse> create(@Valid @RequestBody DeliveryPolicyCreateRequest req) {
        return ResponseEntity.ok(deliveryPolicyService.create(req));
    }

    @GetMapping("/current")
    @Operation(summary = "현재 배송비 정책 조회")
    // @RequiredRole("USER")
    public ResponseEntity<DeliveryPolicyGetResponse> getCurrent() {
        return ResponseEntity.ok(deliveryPolicyService.getCurrent());
    }

    @GetMapping
    @Operation(summary = "모든 배송비 정책 조회")
    // @RequiredRole("ADMIN")
    public ResponseEntity<List<DeliveryPolicyGetResponse>> getAll() {
        List<DeliveryPolicyGetResponse> policies = deliveryPolicyService.getAll();
        return ResponseEntity.ok(policies);
    }

    @PutMapping("/{id}")
    @Operation(summary = "배송비 정책 수정(관리자)")
    // @RequiredRole("ADMIN")
    public ResponseEntity<DeliveryPolicyGetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryPolicyUpdateRequest req) {
        return ResponseEntity.ok(deliveryPolicyService.update(id, req));
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

    @DeleteMapping("/{id}")
    @Operation(summary = "배송비 정책 삭제(관리자)", description = "관리자만 삭제할 수 있습니다.")
    // @RequiredRole("ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryPolicyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 간단 응답 DTO
    public record DeliveryFeeResponse(int orderTotal, int deliveryFee, boolean free) {}

}
