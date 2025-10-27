package store.bookscamp.api.pointhistory.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `point_history` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PointHistory extends SoftDeleteEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(STRING)
    @Column(nullable = false)
    private PointType pointType;

    @Column(nullable = false)
    private Integer pointAmount;

    public PointHistory(OrderInfo orderInfo, Member member, PointType pointType, Integer pointAmount) {
        this.orderInfo = orderInfo;
        this.member = member;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
    }
}
