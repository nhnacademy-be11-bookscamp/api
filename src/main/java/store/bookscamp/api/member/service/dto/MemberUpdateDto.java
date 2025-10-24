package store.bookscamp.api.member.service.dto;

public record MemberUpdateDto(
        String name,
        String email,
        String phone
) {
}
