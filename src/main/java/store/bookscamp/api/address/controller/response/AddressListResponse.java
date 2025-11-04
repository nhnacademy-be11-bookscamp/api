package store.bookscamp.api.address.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import store.bookscamp.api.address.service.dto.AddressReadDto;

public record AddressListResponse(
        List<AddressResponse> addresses
) {
    public static AddressListResponse from(List<AddressReadDto> addressDtos) {
        List<AddressResponse> responses = addressDtos.stream()
                .map(AddressResponse::from)
                .toList();

        return new AddressListResponse(responses);
    }

    public record AddressResponse(
            Long id,
            String label,
            String roadNameAddress,
            Integer zipCode,
            boolean isDefault,
            String detailAddress) {

        public static AddressResponse from(AddressReadDto dto) {
            return new AddressResponse(
                    dto.id(),
                    dto.label(),
                    dto.roadNameAddress(),
                    dto.zipCode(),
                    dto.isDefault(),
                    dto.detailAddress()
            );
        }
    }
}
