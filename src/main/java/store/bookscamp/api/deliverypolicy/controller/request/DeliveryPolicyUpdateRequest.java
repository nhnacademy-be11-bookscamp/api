package store.bookscamp.api.deliverypolicy.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 수정 요청
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeliveryPolicyUpdateRequest {

    @Schema(description = "무료배송 기준 금액(원)")
    @PositiveOrZero
    private Integer freeDeliveryThreshold;

    @Schema(description = "기본 배송비(원)")
    @PositiveOrZero
    private Integer baseDeliveryFee;

    public boolean hasFreeThreshold() { return freeDeliveryThreshold != null; }
    public boolean hasBaseFee() { return baseDeliveryFee != null; }
}
