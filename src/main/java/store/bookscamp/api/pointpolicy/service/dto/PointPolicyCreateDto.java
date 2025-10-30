package store.bookscamp.api.pointpolicy.service.dto;

import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.entity.RewardType;

public record PointPolicyCreateDto(
        PointPolicyType pointPolicyType,
        RewardType rewardType,
        Integer rewardValue
) {
}
