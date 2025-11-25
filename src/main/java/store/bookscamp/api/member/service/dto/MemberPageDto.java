package store.bookscamp.api.member.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import store.bookscamp.api.member.entity.MemberStatus;

public record MemberPageDto(
        Long id,
        String username,
        String name,
        String email,
        String phone,
        MemberStatus status,
        LocalDateTime lastLoginAt,
        LocalDate statusUpdateDate
) {
}
