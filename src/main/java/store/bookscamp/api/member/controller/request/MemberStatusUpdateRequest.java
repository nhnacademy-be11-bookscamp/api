package store.bookscamp.api.member.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.service.dto.MemberStatusUpdateDto;

public record MemberStatusUpdateRequest(
        @NotNull
        Long memberId,

        @NotBlank
        String status
) {
    public static MemberStatusUpdateDto toDto(MemberStatusUpdateRequest request){
        return new MemberStatusUpdateDto(
                request.memberId(),
                MemberStatus.from(request.status())
        );
    }
}
