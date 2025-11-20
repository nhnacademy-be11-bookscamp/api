package store.bookscamp.api.rank.repository.custom;

import java.util.List;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.rank.service.dto.RankSummaryDto;

@Repository
public interface RankRepositoryCustom {

    List<RankSummaryDto> getMemberNetTotalForGrading();
}
