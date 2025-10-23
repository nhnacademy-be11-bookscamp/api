package store.bookscamp.api.deliverypolicy.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class DeliveryPolicy {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer freeDeliveryThreshold;

    @Column(nullable = false)
    private Integer baseDeliveryFee;

    public DeliveryPolicy(Integer freeDeliveryThreshold, Integer baseDeliveryFee) {
        this.freeDeliveryThreshold = freeDeliveryThreshold;
        this.baseDeliveryFee = baseDeliveryFee;
    }
}
