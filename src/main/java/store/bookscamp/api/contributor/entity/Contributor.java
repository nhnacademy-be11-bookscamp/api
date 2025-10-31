package store.bookscamp.api.contributor.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.common.entity.BaseEntity;


@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Contributor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 기여자
    @Column(nullable = false)
    private String contributors;

    // void 고민
    public Contributor(String contributors) {
        this.contributors = contributors;
    }

    public void changeContributors(String contributors) {
        this.contributors = contributors;
    }
}
