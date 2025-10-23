package store.bookscamp.api.couponissue.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.common.entity.BaseEntity;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CouponIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime expiredAt;

    private LocalDateTime usedAt;

    public CouponIssue(Coupon coupon,
                       Member member,
                       LocalDateTime expiredAt,
                       LocalDateTime usedAt
    ) {
        this.coupon = coupon;
        this.member = member;
        this.expiredAt = expiredAt;
        this.usedAt = usedAt;
    }
}
