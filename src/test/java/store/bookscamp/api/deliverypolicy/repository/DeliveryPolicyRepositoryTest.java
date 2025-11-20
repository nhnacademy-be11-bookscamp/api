package store.bookscamp.api.deliverypolicy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

@DataJpaTest
@ActiveProfiles("test")
class DeliveryPolicyRepositoryTest {

    @Autowired
    DeliveryPolicyRepository deliveryPolicyRepository;

    @Test
    @DisplayName("findTopByOrderByIdAsc — 가장 오래된(가장 작은 ID) 정책을 하나 조회한다")
    void findTopByOrderByIdAsc_success() {
        // given
        DeliveryPolicy p1 = new DeliveryPolicy(0, 3000);
        DeliveryPolicy p2 = new DeliveryPolicy(20000, 0);

        DeliveryPolicy saved1 = deliveryPolicyRepository.save(p1); // 먼저 저장 → 더 작은 ID
        DeliveryPolicy saved2 = deliveryPolicyRepository.save(p2); // 나중에 저장 → 더 큰 ID

        // when
        Optional<DeliveryPolicy> result = deliveryPolicyRepository.findTopByOrderByIdAsc();

        // then
        assertThat(result).isPresent();

        DeliveryPolicy oldest = result.get();
        // 가장 먼저 저장한 saved1과 같은지 확인
        assertThat(oldest.getId()).isEqualTo(saved1.getId());
        assertThat(oldest.getFreeDeliveryThreshold()).isEqualTo(0);
        assertThat(oldest.getBaseDeliveryFee()).isEqualTo(3000);

        // 참고로, 두 ID의 대소 관계도 한 번 더 체크해볼 수 있습니다.
        assertThat(saved1.getId()).isLessThan(saved2.getId());
    }


    @Test
    @DisplayName("findTopByOrderByIdAsc — 정책이 없으면 Optional.empty 를 반환한다")
    void findTopByOrderByIdAsc_empty() {
        Optional<DeliveryPolicy> result = deliveryPolicyRepository.findTopByOrderByIdAsc();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("기본 CRUD — 저장 후 조회, 수정, 삭제가 정상 동작한다")
    void crud_basic() {
        DeliveryPolicy policy = new DeliveryPolicy(10000, 2500);
        DeliveryPolicy saved = deliveryPolicyRepository.save(policy);

        assertThat(saved.getId()).isNotNull();

        DeliveryPolicy found = deliveryPolicyRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFreeDeliveryThreshold()).isEqualTo(10000);
        assertThat(found.getBaseDeliveryFee()).isEqualTo(2500);

        found.update(20000, 3000);
        DeliveryPolicy updated = deliveryPolicyRepository.save(found);
        assertThat(updated.getFreeDeliveryThreshold()).isEqualTo(20000);
        assertThat(updated.getBaseDeliveryFee()).isEqualTo(3000);

        deliveryPolicyRepository.delete(updated);
        assertThat(deliveryPolicyRepository.findById(updated.getId())).isEmpty();
    }
}

