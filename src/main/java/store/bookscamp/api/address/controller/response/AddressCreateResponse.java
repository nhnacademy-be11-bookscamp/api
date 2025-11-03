package store.bookscamp.api.address.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressCreateResponse(Long id, String label, @JsonProperty("road_name_address") String roadNameAddress,
                                    String zipCode) {

}
