package store.bookscamp.api.address.service.dto;

public record AddressUpdateRequestDto(
        String label,
        String roadNameAddress,
        Integer zipCode) {
    public AddressUpdateRequestDto {
        if (label != null) {
            label = label.trim();
        }
        if (roadNameAddress != null) {
            roadNameAddress = roadNameAddress.trim();
        }
    }
}
