package store.bookscamp.api.member.controller.response;

import java.time.LocalDate;
import store.bookscamp.api.member.service.dto.MemberGetDto;

public record MemberGetResponse(
        String userName,
        String name,
        String email,
        String phone,
        Integer point,
        LocalDate birthDate
) {
    public static MemberGetResponse fromDto(MemberGetDto memberGetDto){
        return new MemberGetResponse(
                memberGetDto.userName(),
                memberGetDto.name(),
                memberGetDto.email(),
                memberGetDto.phone(),
                memberGetDto.point(),
                memberGetDto.birthDate()
        );
    }
}
