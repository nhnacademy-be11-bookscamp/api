package store.bookscamp.api.member.service.dto;

import store.bookscamp.api.member.entity.MemberStatus;

public record MemberStatusUpdateDto(
        Long memberId,
        MemberStatus status
) {
}
