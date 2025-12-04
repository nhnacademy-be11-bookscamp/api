package store.bookscamp.api.payment.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;
import store.bookscamp.api.orderinfo.entity.OrderInfo;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `payment` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Payment extends SoftDeleteEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderInfo orderInfo;

    @Column(nullable = false)
    private Integer paidAmount;

    @Column
    private LocalDateTime paidAt;

    @Column(unique = true)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider paymentProvider;

    public Payment(
            OrderInfo orderInfo,
            Integer paidAmount,
            LocalDateTime paidAt,
            String paymentKey,
            PaymentMethod paymentMethod,
            PaymentProvider paymentProvider
    ) {
        this.orderInfo = orderInfo;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.paymentProvider = paymentProvider;
    }
}
