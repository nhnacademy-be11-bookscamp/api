package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.orderinfo.entity.ReturnType;
import store.bookscamp.api.orderinfo.service.dto.OrderReturnRequestDto;

public record OrderReturnRequest(
        @NotNull(message = "반품 타입은 필수입니다.")
        ReturnType returnType
) {
    public OrderReturnRequestDto toDto() {
        return new OrderReturnRequestDto(returnType);
    }
}
