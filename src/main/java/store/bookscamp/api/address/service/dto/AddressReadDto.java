package store.bookscamp.api.address.service.dto;

import store.bookscamp.api.address.entity.Address;

public record AddressReadDto(Long id,
                             String label,
                             String roadNameAddress,
                             Integer zipCode,
                             boolean isDefault,
                             String detailAddress) {

    public AddressReadDto {
        if (label != null) {
            label = label.trim();
        }
        if (roadNameAddress != null) {
            roadNameAddress = roadNameAddress.trim();
        }
        if (detailAddress != null) {
            detailAddress = detailAddress.trim();
        }
    }

    public static AddressReadDto from(Address address) {
        return new AddressReadDto(
                address.getId(),
                address.getLabel(),
                address.getRoadNameAddress(),
                address.getZipCode(),
                address.isDefault(),
                address.getDetailAddress()
        );
    }
}
