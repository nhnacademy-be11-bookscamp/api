package store.bookscamp.api.orderinfo.service.dto;

import store.bookscamp.api.orderinfo.entity.ReturnType;

public record OrderReturnRequestDto(
        ReturnType returnType
) {
}
