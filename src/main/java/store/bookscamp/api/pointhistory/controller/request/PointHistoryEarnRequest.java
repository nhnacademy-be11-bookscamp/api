package store.bookscamp.api.pointhistory.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;

public record PointHistoryEarnRequest(

        @NotNull
        Long memberId,

        Long orderId,

        @NotNull @Min(0)
        Integer pointAmount,

        String description
) {

        public PointHistoryEarnDto toDto() {
                return new PointHistoryEarnDto(
                        memberId,
                        orderId,
                        PointType.EARN,
                        pointAmount,
                        description
                );
        }
}
