package store.bookcamp.api.member.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.State;

public record MemberDto(
        Long id,
        String accountId,
        String name,
        String email,
        String phone,
        LocalDate birth,
        Integer point,
        State state,
        LocalDate statusUpdateDate,
        LocalDateTime lastLoginAt
) {
    public static MemberDto fromEntity(Member member) {
        return new MemberDto(
                member.getId(),
                member.getAccountId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getBirth(),
                member.getPoint(),
                member.getState(),
                member.getStatusUpdateDate(),
                member.getLastLoginAt()
        );
    }
}