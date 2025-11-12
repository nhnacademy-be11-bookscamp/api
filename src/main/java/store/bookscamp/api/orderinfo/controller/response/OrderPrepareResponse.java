package store.bookscamp.api.orderinfo.controller.response;

import java.util.List;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;

public record OrderPrepareResponse(
        List<OrderItemInfo> orderItems,
        PriceInfo priceInfo,
        Integer availablePoint,
        List<PackagingInfo> availablePackagings,
        List<CouponInfo> availableCoupons
) {
    public static OrderPrepareResponse fromDto(OrderPrepareDto dto) {
        return new OrderPrepareResponse(
                dto.orderItems().stream()
                        .map(OrderItemInfo::fromDto)
                        .toList(),
                PriceInfo.fromDto(dto.priceInfo()),
                dto.availablePoint(),
                dto.availablePackagings().stream()
                        .map(PackagingInfo::fromDto)
                        .toList(),
                dto.availableCoupons().stream()
                        .map(CouponInfo::fromDto)
                        .toList()
        );
    }
}