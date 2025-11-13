package store.bookscamp.api.deliverypolicy.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class DeliveryPolicy {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int freeDeliveryThreshold;

    @Column(nullable = false)
    private int baseDeliveryFee;

    public DeliveryPolicy(int freeDeliveryThreshold, int baseDeliveryFee) {
        this.freeDeliveryThreshold = freeDeliveryThreshold;
        this.baseDeliveryFee = baseDeliveryFee;
    }

    public void update(Integer freeDeliveryThreshold, Integer baseDeliveryFee) {
        if (freeDeliveryThreshold != null) {
            this.freeDeliveryThreshold = freeDeliveryThreshold;
        }
        if (baseDeliveryFee != null) {
            this.baseDeliveryFee = baseDeliveryFee;
        }
    }
}
