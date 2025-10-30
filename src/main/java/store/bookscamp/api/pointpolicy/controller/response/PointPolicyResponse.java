package store.bookscamp.api.pointpolicy.controller.response;

import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.entity.RewardType;

public record PointPolicyResponse(
        Long pointPolicyId,
        PointPolicyType pointPolicyType,
        RewardType rewardType,
        Integer rewardValue
) {

    public static PointPolicyResponse from(PointPolicy pointPolicy) {
        return new PointPolicyResponse(
                pointPolicy.getId(),
                pointPolicy.getPointPolicyType(),
                pointPolicy.getRewardType(),
                pointPolicy.getRewardValue()
        );
    }
}
