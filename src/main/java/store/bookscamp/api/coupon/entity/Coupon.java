package store.bookscamp.api.coupon.entity;

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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `coupon` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Coupon extends SoftDeleteEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(nullable = false)
    private TargetType targetType;

    private Long targetId; // 카테고리 or 도서 id

    @Enumerated(STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private int discountValue;

    @Column(nullable = false)
    private int minOrderAmount;

    private Integer maxDiscountAmount;

    private Integer validDays; // 쿠폰 유효 기간

    @Column(nullable = false)
    private String name;

    public Coupon(TargetType targetType,
                  Long targetId,
                  DiscountType discountType,
                  int discountValue,
                  int minOrderAmount,
                  Integer maxDiscountAmount,
                  Integer validDays,
                  String name
    ) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validDays = validDays;
        this.name = name;
    }
}
