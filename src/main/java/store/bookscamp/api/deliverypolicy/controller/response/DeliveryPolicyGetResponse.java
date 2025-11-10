package store.bookscamp.api.deliverypolicy.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

/**
 * 조회 응답
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeliveryPolicyGetResponse {

    @Schema(description = "배송비정책 ID", example = "1")
    private Long id;

    @Schema(description = "무료배송 기준 금액(원)", example = "30000")
    private int freeDeliveryThreshold;

    @Schema(description = "기본 배송비(원)", example = "5000")
    private int baseDeliveryFee;

    public static DeliveryPolicyGetResponse fromEntity(DeliveryPolicy policy) {
        return DeliveryPolicyGetResponse.builder()
                .id(policy.getId())
                .freeDeliveryThreshold(policy.getFreeDeliveryThreshold())
                .baseDeliveryFee(policy.getBaseDeliveryFee())
                .build();
    }
}
