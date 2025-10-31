package store.bookscamp.api.pointpolicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.GOLD;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.WELCOME;
import static store.bookscamp.api.pointpolicy.entity.RewardType.AMOUNT;
import static store.bookscamp.api.pointpolicy.entity.RewardType.RATE;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyCreateDto;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyUpdateDto;

@SpringBootTest
@Transactional
class PointPolicyServiceTest {

    @Autowired
    private PointPolicyService pointPolicyService;

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("포인트 정책 생성 성공")
    void createPointPolicy_success() {
        // given
        PointPolicyCreateDto dto = new PointPolicyCreateDto(
                WELCOME,
                AMOUNT,
                100
        );

        // when
        Long id = pointPolicyService.createPointPolicy(dto);

        // then
        PointPolicy found = pointPolicyRepository.findById(id).orElseThrow();
        assertThat(found.getPointPolicyType()).isEqualTo(WELCOME);
        assertThat(found.getRewardType()).isEqualTo(AMOUNT);
        assertThat(found.getRewardValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("포인트 정책 수정 성공")
    void updatePointPolicy_success() {
        // given
        PointPolicy policy = new PointPolicy(WELCOME, AMOUNT, 100);
        pointPolicyRepository.save(policy);

        PointPolicyUpdateDto dto = new PointPolicyUpdateDto(
                policy.getId(),
                WELCOME,
                RATE,
                3
        );

        // when
        pointPolicyService.updatePointPolicy(dto);

        // then
        PointPolicy updated = pointPolicyRepository.findById(policy.getId()).orElseThrow();
        assertThat(updated.getPointPolicyType()).isEqualTo(WELCOME);
        assertThat(updated.getRewardType()).isEqualTo(RATE);
        assertThat(updated.getRewardValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 포인트 정책 수정 시 예외 발생")
    void updatePointPolicy_notFound() {
        PointPolicyUpdateDto dto = new PointPolicyUpdateDto(999L, WELCOME, AMOUNT, 5);
        assertThatThrownBy(() -> pointPolicyService.updatePointPolicy(dto))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("포인트 정책 삭제 성공")
    void deletePointPolicy_success() {
        // given
        PointPolicy policy = new PointPolicy(WELCOME, AMOUNT, 100);
        pointPolicyRepository.save(policy);

        // when
        pointPolicyService.deletePointPolicy(policy.getId());

        // then
        assertThat(pointPolicyRepository.findById(policy.getId())).isEmpty();
    }

    @Test
    @DisplayName("포인트 정책 단건 조회 성공")
    void getPointPolicy_success() {
        // given
        PointPolicy policy = new PointPolicy(WELCOME, AMOUNT, 100);
        pointPolicyRepository.save(policy);

        // when
        PointPolicy found = pointPolicyService.getPointPolicy(policy.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getPointPolicyType()).isEqualTo(WELCOME);
    }

    @Test
    @DisplayName("모든 포인트 정책 조회 성공")
    void listPointPolicies_success() {
        // given
        pointPolicyRepository.save(new PointPolicy(WELCOME, AMOUNT, 100));
        pointPolicyRepository.save(new PointPolicy(GOLD, RATE, 5));

        // when
        List<PointPolicy> result = pointPolicyService.listPointPolicies();

        // then
        assertThat(result).hasSize(2);
    }
}
