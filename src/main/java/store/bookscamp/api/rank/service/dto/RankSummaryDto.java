package store.bookscamp.api.rank.service.dto;

import java.math.BigDecimal;

public record RankSummaryDto(

        Long memberId,
        BigDecimal totalNetAmount
) {
}
