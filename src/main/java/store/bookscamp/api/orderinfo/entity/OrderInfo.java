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
import store.bookscamp.api.couponissue.entity.CouponIssue;
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
    @JoinColumn(name = "coupon_issue_id")
    private CouponIssue couponIssue;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Column(nullable = false)
    private Integer netAmount; // 순수 도서 금액 합계

    @Column(nullable = false)
    private Integer totalAmount; // 총 주문 금액 (도서 + 포장비 + 배송비)

    @Column(nullable = false)
    private Integer deliveryFee; // 배송비

    @Column(nullable = false)
    private Integer packagingFee; // 포장비

    @Column(nullable = false)
    private Integer discountAmount; // 쿠폰 할인 금액

    @Column(nullable = false)
    private Integer finalPaymentAmount; // 최종 결제 금액 (총액 - 할인 - 포인트)

    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private int usedPoint;

    public OrderInfo(Member member,
                 CouponIssue couponIssue,
                 Delivery delivery,
                 Integer netAmount,
                 Integer totalAmount,
                 Integer deliveryFee,
                 Integer packagingFee,
                 Integer discountAmount,
                 Integer finalPaymentAmount,
                 OrderStatus orderStatus,
                 int usedPoint
    ) {
        this.member = member;
        this.couponIssue = couponIssue;
        this.delivery = delivery;
        this.netAmount = netAmount;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.packagingFee = packagingFee;
        this.discountAmount = discountAmount;
        this.finalPaymentAmount = finalPaymentAmount;
        this.orderStatus = orderStatus;
        this.usedPoint = usedPoint;
    }

    public void changeOrderStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }
}
