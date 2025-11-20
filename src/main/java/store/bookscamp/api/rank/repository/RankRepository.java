package store.bookscamp.api.rank.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.custom.RankRepositoryCustom;

public interface RankRepository extends JpaRepository<Rank, Long>, RankRepositoryCustom {

    Optional<Rank> findByPointPolicy_PointPolicyType(PointPolicyType pointPolicyType);
}
