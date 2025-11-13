package store.bookscamp.api.orderinfo.service.dto;

import java.util.List;

public record OrderRequestDto(
        List<OrderItemCreateDto> items,
        DeliveryInfoDto deliveryInfo,
        Long couponIssueId,
        Integer usedPoint,
        NonMemberInfoDto nonMemberInfo
) {
}