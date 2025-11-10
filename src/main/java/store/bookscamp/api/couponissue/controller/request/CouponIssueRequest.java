package store.bookscamp.api.couponissue.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record CouponIssueRequest(

        Long couponId
) {
}