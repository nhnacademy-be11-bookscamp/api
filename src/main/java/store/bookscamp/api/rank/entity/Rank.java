package store.bookscamp.api.rank.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;

@Entity
@Table(name = "member_rank")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Rank {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "point_policy_id")
    private PointPolicy pointPolicy;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int cumulativeMinAmount;

    @Column(nullable = false)
    private int cumulativeMaxAmount;

    public Rank(PointPolicy pointPolicy, String name, Integer cumulativeMinAmount, Integer cumulativeMaxAmount) {
        this.pointPolicy = pointPolicy;
        this.name = name;
        this.cumulativeMinAmount = cumulativeMinAmount;
        this.cumulativeMaxAmount = cumulativeMaxAmount;
    }

    public boolean contains(int amount) {
        int max = this.cumulativeMaxAmount;

        return amount >= this.cumulativeMinAmount && amount < max;
    }
}
