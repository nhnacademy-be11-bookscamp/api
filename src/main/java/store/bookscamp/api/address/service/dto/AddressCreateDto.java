package store.bookscamp.api.address.service.dto;

public record AddressCreateDto(String Label, String roadNameAddress, Integer zipCode) {
    public AddressCreateDto {
        if (Label != null) {
            Label = Label.trim();
        }
        if (roadNameAddress != null) {
            roadNameAddress = roadNameAddress.trim();
        }
    }
}
