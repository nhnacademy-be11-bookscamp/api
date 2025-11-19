package store.bookscamp.api.orderinfo.controller.response;

import java.time.LocalDateTime;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;

public record OrderListResponse (
        Long orderId,
        LocalDateTime orderDate,
        String representationBookTitle,
        int totalQuantity,
        int finalPaymentAmount
        ) {

    public static OrderListResponse fromDto(OrderListDto dto) {
        return new OrderListResponse(
                dto.getOrderId(),
                dto.getOrderDate(),
                dto.getRepresentativeBookTitle(),
                dto.getTotalQuantity(),
                dto.getFinalPaymentAmount()
        );
    }
}
