package store.bookscamp.api.member.service;

import java.time.LocalDate;
import store.bookscamp.api.member.entity.Member;

public record MemberGetDto(
        String name,
        String email,
        String phone,
        LocalDate birthDate
) {
    public static MemberGetDto fromEntity(Member member){
        return new MemberGetDto(
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getBirthDate()
        );
    }
}
