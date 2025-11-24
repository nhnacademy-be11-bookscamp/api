package store.bookscamp.api.member.controller.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberPageResponse(
        Long id,
        String username,
        String name,
        String email,
        String phone,
        String status,
        LocalDateTime lastLoginAt,
        LocalDate statusUpdateDate
) {
}
