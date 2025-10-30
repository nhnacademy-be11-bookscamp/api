package store.bookscamp.api.pointpolicy.controller.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.entity.RewardType;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyUpdateDto;

public record PointPolicyUpdateRequest(

        @NotNull
        PointPolicyType pointPolicyType,

        @NotNull
        RewardType rewardType,

        @NotNull
        @Positive
        Integer rewardValue
) {

    public PointPolicyUpdateDto toDto(Long pointPolicyId) {
        return new PointPolicyUpdateDto(
                pointPolicyId,
                pointPolicyType,
                rewardType,
                rewardValue
        );
    }
}
