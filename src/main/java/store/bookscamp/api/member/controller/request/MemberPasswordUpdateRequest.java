package store.bookscamp.api.member.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import store.bookscamp.api.member.service.MemberPasswordUpdateDto;

public record MemberPasswordUpdateRequest(
        @NotNull
        @Size(min = 8, max = 20)
        String password
){
    public static MemberPasswordUpdateDto toDto(MemberPasswordUpdateRequest member){
        return new MemberPasswordUpdateDto(
                member.password()
        );
    }
}
