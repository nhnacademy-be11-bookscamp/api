package store.bookscamp.api.rank.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Rank {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_policy_id")
    private PointPolicy pointPolicy;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer cumulativeMinAmount;

    @Column(nullable = false)
    private Integer cumulativeMaxAmount;

    public Rank(PointPolicy pointPolicy, String name, Integer cumulativeMinAmount, Integer cumulativeMaxAmount) {
        this.pointPolicy = pointPolicy;
        this.name = name;
        this.cumulativeMinAmount = cumulativeMinAmount;
        this.cumulativeMaxAmount = cumulativeMaxAmount;
    }
}
