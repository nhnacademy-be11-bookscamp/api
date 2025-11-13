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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `coupon_issue` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class CouponIssue extends SoftDeleteEntity {

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

    public CouponIssue(Coupon coupon,
                       Member member,
                       LocalDateTime expiredAt
    ) {
        this.coupon = coupon;
        this.member = member;
        this.expiredAt = expiredAt;
    }

    public void use() {
        if (this.usedAt != null) {
            throw new ApplicationException(ErrorCode.COUPON_ALREADY_USED);
        }
        if (this.expiredAt != null && LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new ApplicationException(ErrorCode.COUPON_EXPIRED);
        }
        this.usedAt = LocalDateTime.now();
    }
}
