package store.bookscamp.api.address.service.dto;

public record AddressCreateDto(String label, String roadNameAddress, Integer zipCode) {
    public AddressCreateDto {
        if (label != null) {
            label = label.trim();
        }
        if (roadNameAddress != null) {
            roadNameAddress = roadNameAddress.trim();
        }
    }
}
