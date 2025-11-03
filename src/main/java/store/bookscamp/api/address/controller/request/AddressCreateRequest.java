package store.bookscamp.api.address.controller.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.address.service.dto.AddressCreateDto;

public record AddressCreateRequest(String label, @NotBlank @JsonProperty("road_name_address") String roadNameAddress,
                                   @NotNull @JsonProperty("zip_code") Integer zipCode) {

    public static AddressCreateDto toDto(AddressCreateRequest addressCreateRequest) {
        return new AddressCreateDto(
                addressCreateRequest.label,
                addressCreateRequest.roadNameAddress,
                addressCreateRequest.zipCode
        );
    }
}
