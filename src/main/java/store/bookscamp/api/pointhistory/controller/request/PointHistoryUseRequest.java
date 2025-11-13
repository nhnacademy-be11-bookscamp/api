package store.bookscamp.api.pointhistory.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryUseDto;

public record PointHistoryUseRequest(

        @NotNull
        Long memberId,

        Long orderId,

        @NotNull @Min(0)
        Integer pointAmount
) {

        public PointHistoryUseDto toDto() {
                return new PointHistoryUseDto(
                        memberId,
                        orderId,
                        PointType.USE,
                        pointAmount
                );
        }
}
