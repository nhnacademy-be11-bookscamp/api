package store.bookcamp.api.member.controller.response;

import java.time.LocalDate;
import store.bookcamp.api.member.service.MemberGetDto;

public record MemberGetResponse(
        String name,
        String email,
        String phone,
        LocalDate birthDate
) {
    public static MemberGetResponse fromDto(MemberGetDto memberGetDto){
        return new MemberGetResponse(
                memberGetDto.name(),
                memberGetDto.email(),
                memberGetDto.phone(),
                memberGetDto.birthDate()
        );
    }
}
