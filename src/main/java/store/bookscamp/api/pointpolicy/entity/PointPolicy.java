package store.bookscamp.api.pointpolicy.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PointPolicy {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(nullable = false)
    private PointPolicyType pointPolicyType;

    @Enumerated(STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    @Column(nullable = false)
    private Integer rewardPoint;

    public PointPolicy(PointPolicyType pointPolicyType, RewardType rewardType, Integer rewardPoint) {
        this.pointPolicyType = pointPolicyType;
        this.rewardType = rewardType;
        this.rewardPoint = rewardPoint;
    }
}
