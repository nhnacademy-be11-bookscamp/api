package store.bookscamp.api.address.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import java.util.List;

public record AddressListResponse(
        @JsonProperty("addresses") List<AddressResponse> addresses
) {

    public static AddressListResponse from(List<AddressReadDto> addressDtos) {
        List<AddressResponse> responses = addressDtos.stream()
                .map(AddressResponse::from)
                .toList();

        return new AddressListResponse(responses);
    }

    public record AddressResponse(
            String label,
            @JsonProperty("road_name_address") String roadNameAddress,
            @JsonProperty("zip_code") Integer zipCode
    ) {
        public static AddressResponse from(AddressReadDto dto) {
            return new AddressResponse(
                    dto.label(),
                    dto.roadNameAddress(),
                    dto.zipCode()
            );
        }
    }
}
