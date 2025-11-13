package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import store.bookscamp.api.orderinfo.service.dto.DeliveryInfoDto;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderRequestDto;

public record OrderCreateRequest(
        @NotEmpty(message = "주문 도서는 최소 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemCreateRequest> items,

        @NotNull(message = "배송 정보는 필수입니다.")
        @Valid
        DeliveryInfoRequest deliveryInfo,

        Long couponIssueId,

        @Min(value = 0, message = "포인트는 0 이상이어야 합니다.")
        Integer usedPoint,

        @Valid
        NonMemberInfoRequest nonMemberInfo
) {
    public OrderRequestDto toDto() {
        return new OrderRequestDto(
                items.stream()
                        .map(item -> new OrderItemCreateDto(
                                item.bookId(),
                                item.quantity(),
                                item.packagingId()
                        ))
                        .toList(),
                new DeliveryInfoDto(
                        deliveryInfo.recipientName(),
                        deliveryInfo.recipientPhone(),
                        deliveryInfo.zipCode(),
                        deliveryInfo.roadNameAddress(),
                        deliveryInfo.detailAddress(),
                        deliveryInfo.desiredDeliveryDate(),
                        deliveryInfo.deliveryMemo()
                ),
                couponIssueId,
                usedPoint,
                nonMemberInfo != null ? new NonMemberInfoDto(nonMemberInfo.password()) : null
        );
    }
}