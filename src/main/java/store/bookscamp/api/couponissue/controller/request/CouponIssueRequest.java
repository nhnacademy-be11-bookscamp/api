package store.bookscamp.api.couponissue.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CouponIssueRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long couponId;

    // 선택적
    private Long categoryId;

    // 선택적
    private Long bookId;
}