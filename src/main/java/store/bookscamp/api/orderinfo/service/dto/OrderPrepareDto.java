package store.bookscamp.api.orderinfo.service.dto;

import java.util.List;

public record OrderPrepareDto(
        List<OrderItemDto> orderItems,
        PriceDto priceInfo,
        Integer availablePoint,
        List<PackagingDto> availablePackagings,
        List<CouponDto> availableCoupons
) {
}