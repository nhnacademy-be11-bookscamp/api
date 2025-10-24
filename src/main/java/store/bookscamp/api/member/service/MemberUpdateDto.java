package store.bookscamp.api.member.service;

public record MemberUpdateDto(
        String name,
        String email,
        String phone
) {
}
