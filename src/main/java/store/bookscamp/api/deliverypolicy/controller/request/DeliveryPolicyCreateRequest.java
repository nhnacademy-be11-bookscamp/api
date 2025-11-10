package store.bookscamp.api.deliverypolicy.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

/**
 * 관리자 생성 요청
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeliveryPolicyCreateRequest {

    @Schema(description = "무료배송 기준 금액(원)", example = "30000")
    @NotNull
    @PositiveOrZero
    private int freeDeliveryThreshold;

    @Schema(description = "기본 배송비(원)", example = "5000")
    @NotNull @PositiveOrZero
    private Integer baseDeliveryFee;

    public DeliveryPolicy toEntity() {
        return new DeliveryPolicy(freeDeliveryThreshold, baseDeliveryFee);
    }
}
