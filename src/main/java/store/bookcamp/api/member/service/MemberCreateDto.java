package store.bookcamp.api.member.service;

import java.time.LocalDate;

public record MemberCreateDto(
        String accountId,
        String password,
        String name,
        String email,
        String phone,
        LocalDate birthDate
) {
}