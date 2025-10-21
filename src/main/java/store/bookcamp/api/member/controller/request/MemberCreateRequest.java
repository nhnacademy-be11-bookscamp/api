package store.bookcamp.api.member.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MemberCreateRequest(
        @NotNull
        @Size(min=4, max = 20)
        String id,
        @NotNull
        @Size(min = 8, max = 20)
        String password,
        @NotBlank
        String name,
        @NotBlank @Email
        String email,
        @NotBlank
        String phone,
        @NotNull
        LocalDate birthDate
) {
}