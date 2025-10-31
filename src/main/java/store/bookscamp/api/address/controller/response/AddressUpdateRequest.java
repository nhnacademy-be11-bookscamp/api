package store.bookscamp.api.address.controller.response;

import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;

public record AddressUpdateRequest(
        String label,
        String roadNameAddress,
        Integer zipCode
) {
    public static AddressUpdateRequestDto toDto(AddressUpdateRequest request) {
        return new AddressUpdateRequestDto(
                request.label,
                request.roadNameAddress,
                request.zipCode
        );
    }
}
