package store.bookscamp.api.orderinfo.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.delivery.entity.Delivery;
import store.bookscamp.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `order_info` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OrderInfo extends SoftDeleteEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Column(nullable = false)
    private Integer netAmount; // 순수 금액

    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private int usedPoint;

    public OrderInfo(Member member,
                 Coupon coupon,
                 Delivery delivery,
                 Integer netAmount,
                 OrderStatus orderStatus,
                 int usedPoint
    ) {
        this.member = member;
        this.coupon = coupon;
        this.delivery = delivery;
        this.netAmount = netAmount;
        this.orderStatus = orderStatus;
        this.usedPoint = usedPoint;
    }
}
