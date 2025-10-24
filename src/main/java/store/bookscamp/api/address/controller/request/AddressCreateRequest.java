package store.bookscamp.api.address.controller.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import store.bookscamp.api.address.service.dto.AddressCreateDto;

public record AddressCreateRequest(String label, @NotBlank @JsonProperty("road_name_address") String address,
                                   @NotBlank Integer zipCode) {

    public static AddressCreateDto toDto(AddressCreateRequest addressCreateRequest) {
        return new AddressCreateDto(
                addressCreateRequest.label,
                addressCreateRequest.address,
                addressCreateRequest.zipCode
        );
    }
}
