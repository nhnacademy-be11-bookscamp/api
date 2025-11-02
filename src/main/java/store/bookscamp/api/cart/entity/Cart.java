package store.bookscamp.api.cart.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    public Cart(Member member) {
        this.member = member;
    }
}
