package store.bookscamp.api.couponissue.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.couponissue.controller.request.CouponIssueRequest;
import store.bookscamp.api.couponissue.controller.response.CouponIssueResponse;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.service.CouponIssueService;

@RestController
@RequestMapping("/coupon-issues")
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    @PostMapping("/issue")
    @RequiredRole("USER")
    public ResponseEntity<Long> issueCoupon(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CouponIssueRequest couponIssueRequest
    ) {

        Long issuedCouponId = couponIssueService.issueGeneralCoupon(
                couponIssueRequest.couponId(),
                Long.valueOf(httpServletRequest.getHeader("X-User-ID"))
                );

        return ResponseEntity.ok(issuedCouponId);
    }

    @GetMapping("/my")
    @RequiredRole("USER")
    public ResponseEntity<List<CouponIssueResponse>> getMyCoupons(HttpServletRequest request) {
        Long memberId = Long.valueOf(request.getHeader("X-User_ID"));
        List<CouponIssue> couponIssues = couponIssueService.listCouponIssue(memberId);

        List<CouponIssueResponse> response = couponIssues.stream()
                .map(CouponIssueResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{couponIssueId}")
    @RequiredRole("USER")
    public ResponseEntity<Void> deleteCouponIssue(
            @PathVariable Long couponIssueId
            ) {
        couponIssueService.deleteCouponIssue(couponIssueId);

        return ResponseEntity.ok().build();
    }
}