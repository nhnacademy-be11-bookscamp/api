package store.bookscamp.api.coupon.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.coupon.controller.request.CouponCreateRequest;
import store.bookscamp.api.coupon.controller.response.CouponResponse;
import store.bookscamp.api.coupon.service.CouponService;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "쿠폰 API", description = "Coupon API입니다.")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<Void> createCoupon(
            @RequestBody @Valid CouponCreateRequest request
    ) {
        couponService.createCoupon(request.toDto());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> listCoupons() {
        List<CouponResponse> response = couponService.listCoupons().stream()
                .map(CouponResponse::from)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok().build();
    }
}
