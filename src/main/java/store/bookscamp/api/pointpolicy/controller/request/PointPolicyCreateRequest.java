package store.bookscamp.api.pointpolicy.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.entity.RewardType;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyCreateDto;

public record PointPolicyCreateRequest(

        @NotNull
        PointPolicyType pointPolicyType,

        @NotNull
        RewardType rewardType,

        @NotNull
        @Positive
        Integer rewardValue
) {

    public PointPolicyCreateDto toDto() {
        return new PointPolicyCreateDto(
                pointPolicyType,
                rewardType,
                rewardValue
        );
    }
}
