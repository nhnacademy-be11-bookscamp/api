package store.bookcamp.api.member.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import store.bookcamp.api.member.service.MemberUpdateDto;

public record MemberUpdateRequest(
        @NotBlank
        String name,
        @NotBlank @Email
        String email,
        @NotBlank
        String phone
) {
    public static MemberUpdateDto toDto(MemberUpdateRequest member){
        return new MemberUpdateDto(
                member.name(),
                member.email(),
                member.phone()
        );
    }
}
