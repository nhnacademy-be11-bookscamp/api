package store.bookcamp.api.member.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.Status;

public record MemberDto(
        Long id,
        String accountId,
        String password,
        String name,
        String email,
        String phone,
        LocalDate birthDate,
        Integer point,
        Status state,
        LocalDate statusUpdateDate,
        LocalDateTime lastLoginAt
        ) {
    public MemberDto(String accountId, String password, String name, String email,String phone, LocalDate birthDate) {
        this(
                null,
                accountId,
                password,
                name,
                email,
                phone,
                birthDate,
                0,
                Status.NORMAL,
                LocalDate.now(),
                null
        );
    }
    public static MemberDto fromEntity(Member member) {
        return new MemberDto(
                member.getId(),
                member.getAccountId(),
                member.getPassword(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getBirthDate(),
                member.getPoint(),
                member.getStatus(),
                member.getStatusUpdateDate(),
                member.getLastLoginAt()
        );
    }
}