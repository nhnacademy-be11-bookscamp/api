package store.bookscamp.api.pointhistory.controller.response;

import java.time.LocalDateTime;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;

public record PointHistoryResponse(
        Long id,
        Long orderId,
        PointType pointType,
        Integer pointAmount,
        String description,
        LocalDateTime createdAt
) {
    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
                pointHistory.getId(),
                pointHistory.getOrderInfo() != null ? pointHistory.getOrderInfo().getId() : null,
                pointHistory.getPointType(),
                pointHistory.getPointAmount(),
                pointHistory.getDescription(),
                pointHistory.getCreatedAt()
        );
    }
}