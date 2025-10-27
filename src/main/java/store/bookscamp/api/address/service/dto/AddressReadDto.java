package store.bookscamp.api.address.service.dto;

import store.bookscamp.api.address.entity.Address;

public record AddressReadDto(String label, String roadNameAddress, Integer zipCode) {
    public static AddressReadDto from(Address address) {
        return new AddressReadDto(
                address.getLabel(),
                address.getRoadNameAddress(),
                address.getZipCode()
        );
    }
}
