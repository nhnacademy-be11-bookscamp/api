package store.bookscamp.api.deliverypolicy.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 등록 요청
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPolicyCreateRequest {

    @Schema(description = "무료배송 기준 금액(원)")
    @NotNull
    @PositiveOrZero
    private Integer freeDeliveryThreshold;

    @Schema(description = "기본 배송비(원)")
    @NotNull
    @PositiveOrZero
    private Integer baseDeliveryFee;

}
