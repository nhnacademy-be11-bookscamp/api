package store.bookscamp.api.coupon.query.dto;

import java.util.List;

public record CouponSearchConditionDto(
        Long memberId,
        List<Long> categoryIds,
        List<Long> bookIds
) {
}
