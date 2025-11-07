package store.bookscamp.api.member.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import store.bookscamp.api.member.service.dto.MemberCreateDto;

public record MemberCreateRequest(
        @NotNull
        @Size(min=4, max = 20)
        String username,
        @NotBlank
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
    public static MemberCreateDto toDto(MemberCreateRequest memberCreateRequest){
        return new MemberCreateDto(
                memberCreateRequest.username(),
                memberCreateRequest.password(),
                memberCreateRequest.name(),
                memberCreateRequest.email(),
                memberCreateRequest.phone(),
                memberCreateRequest.birthDate()
        );
    }
}