package store.bookscamp.api.delivery.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Delivery {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "delivery_policy_id")
    private DeliveryPolicy deliveryPolicy;

    private LocalDate shippingDate;

    private LocalDate desiredDeliveryDate;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String recipientName;

    public Delivery(DeliveryPolicy deliveryPolicy,
                    LocalDate shippingDate,
                    LocalDate desiredDeliveryDate,
                    String deliveryAddress,
                    String recipientName
    ) {
        this.deliveryPolicy = deliveryPolicy;
        this.shippingDate = shippingDate;
        this.desiredDeliveryDate = desiredDeliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
    }
}
