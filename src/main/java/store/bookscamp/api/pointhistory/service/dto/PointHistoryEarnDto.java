package store.bookscamp.api.pointhistory.service.dto;

import store.bookscamp.api.pointhistory.entity.PointType;

public record PointHistoryEarnDto(
        Long memberId,
        Long orderId,
        PointType pointType,
        Integer pointAmount,
        String description
) {}
