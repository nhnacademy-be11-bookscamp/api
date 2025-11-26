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
    private Long orderId; // TODO : String orderNumber로 변경해야되나?
    // 변경 했었는데 매핑 문제 생긴거 같아서 일단은 빽뺶뱪뱪

    @OneToOne(fetch = LAZY)
    @MapsId
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @Column(name = "password", nullable = false)
    private String password; // 주문 조회용 비밀번호

    public NonMember(OrderInfo orderInfo, String password) {
        this.orderInfo = orderInfo;
        this.password = password;
    }
}
