package store.bookscamp.api.address.service.dto;

public record AddressUpdateRequestDto(
        String label,
        String roadNameAddress,
        Integer zipCode,
        boolean isDefault,
        String detailAddress) {

    public AddressUpdateRequestDto {
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
}
