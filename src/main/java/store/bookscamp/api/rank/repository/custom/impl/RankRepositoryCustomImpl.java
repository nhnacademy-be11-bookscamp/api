package store.bookscamp.api.rank.repository.custom.impl;

import static store.bookscamp.api.orderinfo.entity.QOrderInfo.orderInfo;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.rank.repository.custom.RankRepositoryCustom;
import store.bookscamp.api.rank.service.dto.RankSummaryDto;

public class RankRepositoryCustomImpl implements RankRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public RankRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<RankSummaryDto> getMemberNetTotalForGrading() {

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        return queryFactory
                .select(
                        Projections.constructor(
                                RankSummaryDto.class,
                                orderInfo.member.id,
                                orderInfo.netAmount.sum().coalesce(0)
                        )
                )
                .from(orderInfo)
                .where(
                        orderInfo.orderStatus.eq(OrderStatus.DELIVERED),
                        orderInfo.createdAt.goe(threeMonthsAgo),
                        orderInfo.deletedAt.isNull()
                )
                .groupBy(orderInfo.member.id)
                .fetch();
    }
}
