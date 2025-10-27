package store.bookscamp.api.address.controller.response;

public record AddressResponse(
        String label,
        String roadNameAddress,
        Integer zipCode
        ) {
}
