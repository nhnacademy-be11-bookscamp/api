package store.bookscamp.api.deliverypolicy.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeliveryPolicyTest {

    @Test
    @DisplayName("update - 값이 정상적으로 변경된다")
    void update_success() {
        // given
        DeliveryPolicy policy = new DeliveryPolicy(30000, 3000);

        // when
        policy.update(50000, 2500);

        // then
        assertThat(policy.getFreeDeliveryThreshold()).isEqualTo(50000);
        assertThat(policy.getBaseDeliveryFee()).isEqualTo(2500);
    }

    @Test
    @DisplayName("update - null 값이 들어오면 기존 값 유지")
    void update_nullValues_keepOriginal() {
        // given
        DeliveryPolicy policy = new DeliveryPolicy(30000, 3000);

        // when
        policy.update(null, null);

        // then
        assertThat(policy.getFreeDeliveryThreshold()).isEqualTo(30000);
        assertThat(policy.getBaseDeliveryFee()).isEqualTo(3000);
    }

    @Test
    @DisplayName("update - 일부 값만 변경되는 경우 나머지는 유지된다")
    void update_partialChange() {
        // given
        DeliveryPolicy policy = new DeliveryPolicy(30000, 3000);

        // when
        policy.update(40000, null);

        // then
        assertThat(policy.getFreeDeliveryThreshold()).isEqualTo(40000);
        assertThat(policy.getBaseDeliveryFee()).isEqualTo(3000);
    }
}
