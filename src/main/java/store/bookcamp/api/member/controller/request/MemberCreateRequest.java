package store.bookcamp.api.member.controller.request;

import java.time.LocalDate;

public record MemberCreateRequest(
        String id,
        String password,
        String name,
        String email,
        String phone,
        LocalDate birth
) {
}