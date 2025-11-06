package store.bookscamp.api.couponissue.query.dto;

import java.util.List;

public record CouponSearchConditionDto(
        Long memberId,
        List<Long> categoryIds,
        List<Long> bookIds
) {
}
