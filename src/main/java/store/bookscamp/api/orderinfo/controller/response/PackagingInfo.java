package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.PackagingDto;

public record PackagingInfo(
        Long id,
        String name,
        Integer price
) {
    public static PackagingInfo fromDto(PackagingDto dto) {
        return new PackagingInfo(
                dto.id(),
                dto.name(),
                dto.price()
        );
    }
}