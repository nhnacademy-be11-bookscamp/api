package store.bookscamp.api.member.service.dto;

import java.time.LocalDate;

public record MemberCreateDto(
        String userName,
        String password,
        String name,
        String email,
        String phone,
        LocalDate birthDate
) {
}