package store.bookscamp.api.deliverypolicy.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyCreateRequest;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.service.DeliveryPolicyService;

@RestController
@Tag(name = "배송비정책 API")
@RequestMapping("/admin/delivery-policy")
@RequiredArgsConstructor
public class DeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;

    @PostMapping
    @Operation(summary = "배송비 정책 등록(관리자)")
    @RequiredRole("ADMIN")
    public ResponseEntity<DeliveryPolicyGetResponse> create (
            @Valid @RequestBody DeliveryPolicyCreateRequest req) {
        return ResponseEntity.ok(deliveryPolicyService.create(req));
    }


    @GetMapping
    @Operation(summary = "현재 배송비 정책 조회")
    @RequiredRole("ADMIN")
    public ResponseEntity<DeliveryPolicyGetResponse> getDeliveryPolicy() {
        return ResponseEntity.ok(deliveryPolicyService.getDeliveryPolicy());
    }

    @PutMapping
    @Operation(summary = "배송비 정책 수정(관리자)")
    @RequiredRole("ADMIN")
    public ResponseEntity<DeliveryPolicyGetResponse> update(
            @Valid @RequestBody DeliveryPolicyUpdateRequest req) {
        return ResponseEntity.ok(deliveryPolicyService.update(req));
    }
}
