package store.bookscamp.api.nonmember.entity;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.orderinfo.entity.OrderInfo;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "non_member")
public class NonMember {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @OneToOne(fetch = LAZY)
    @MapsId
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @Column(name = "password", nullable = false)
    private Integer password; // 암호화된 비밀번호

    public NonMember(OrderInfo orderInfo, Integer password) {
        this.orderInfo = orderInfo;
        this.password = password;
    }
}
