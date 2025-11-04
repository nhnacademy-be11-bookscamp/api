package store.bookscamp.api.address.service.dto;

public record AddressCreateDto(String label,
                               String roadNameAddress,
                               Integer zipCode,
                               boolean isDefault,
                               String detailAddress) {
    public AddressCreateDto {
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
