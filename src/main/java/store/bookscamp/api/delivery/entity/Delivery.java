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
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    @Column(nullable = false)
    private Integer zipCode;

    @Column(nullable = false)
    private String roadNameAddress;

    @Column(nullable = false)
    private String detailAddress;

    private String deliveryMemo;

    public Delivery(DeliveryPolicy deliveryPolicy,
                    LocalDate shippingDate,
                    LocalDate desiredDeliveryDate,
                    String recipientName,
                    String recipientPhone,
                    Integer zipCode,
                    String roadNameAddress,
                    String detailAddress,
                    String deliveryMemo
    ) {
        this.deliveryPolicy = deliveryPolicy;
        this.shippingDate = shippingDate;
        this.desiredDeliveryDate = desiredDeliveryDate;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.roadNameAddress = roadNameAddress;
        this.detailAddress = detailAddress;
        this.deliveryMemo = deliveryMemo;
    }
}
