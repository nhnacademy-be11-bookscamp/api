package store.bookscamp.api.member.service.dto;

import java.time.LocalDate;
import store.bookscamp.api.member.entity.Member;

public record MemberGetDto(
        String username,
        String name,
        String email,
        String phone,
        Integer point,
        LocalDate birthDate
) {
    public static MemberGetDto fromEntity(Member member){
        return new MemberGetDto(
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getPoint(),
                member.getBirthDate()
        );
    }
}
