package store.bookscamp.api.couponissue.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.couponissue.controller.request.CouponIssueRequest;
import store.bookscamp.api.couponissue.service.CouponIssueService;

@RestController
@RequestMapping("/coupon-issues")
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    @PostMapping("/issue")
    public ResponseEntity<Long> issueCoupon(@Valid @RequestBody CouponIssueRequest request) {

        Long issuedCouponId = couponIssueService.issueGeneralCoupon(
                request.getCouponId(), request.getMemberId()
        );

        return ResponseEntity.ok(issuedCouponId);
    }
}