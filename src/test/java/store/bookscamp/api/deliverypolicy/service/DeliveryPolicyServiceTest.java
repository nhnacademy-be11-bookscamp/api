package store.bookscamp.api.deliverypolicy.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyCreateRequest;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryPolicyServiceTest {

    @InjectMocks
    private DeliveryPolicyService deliveryPolicyService;

    @Mock
    private DeliveryPolicyRepository deliveryPolicyRepository;

    private final int freeDeliveryThreshold = 30000;
    private final int baseDeliveryFee = 3000;

    @Test
    @DisplayName("배송 정책을 성공적으로 생성한다")
    void create_success() {
        DeliveryPolicyCreateRequest req = new DeliveryPolicyCreateRequest(freeDeliveryThreshold, baseDeliveryFee);
        DeliveryPolicy newPolicy = new DeliveryPolicy(freeDeliveryThreshold, baseDeliveryFee);

        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(deliveryPolicyRepository.save(any(DeliveryPolicy.class))).thenReturn(newPolicy);

        DeliveryPolicyGetResponse response = deliveryPolicyService.create(req);

        assertThat(response.getFreeDeliveryThreshold()).isEqualTo(freeDeliveryThreshold);
        assertThat(response.getBaseDeliveryFee()).isEqualTo(baseDeliveryFee);
        verify(deliveryPolicyRepository).save(any(DeliveryPolicy.class)); // save 메서드 호출 검증
    }

    @Test
    @DisplayName("이미 정책이 존재할 경우 생성 시도 시 예외가 발생한다")
    void create_policyAlreadyExists_throwsException() {
        // given
        DeliveryPolicyCreateRequest req = new DeliveryPolicyCreateRequest(freeDeliveryThreshold, baseDeliveryFee);
        DeliveryPolicy existingPolicy = new DeliveryPolicy(0, 5000);

        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(existingPolicy));

        // when & then
        assertThatThrownBy(() -> deliveryPolicyService.create(req))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ErrorCode.DELIVERY_POLICY_ALREADY_EXISTS.getMessage());
    }


    @Test
    @DisplayName("배송 정책을 성공적으로 수정한다")
    void update_success() {
        // given
        int newFreeDeliveryThreshold = 50000;
        int newBaseDeliveryFee = 2500;
        DeliveryPolicyUpdateRequest req = new DeliveryPolicyUpdateRequest(newFreeDeliveryThreshold, newBaseDeliveryFee);

        // 기존 정책 객체
        DeliveryPolicy existingPolicy = new DeliveryPolicy(freeDeliveryThreshold, baseDeliveryFee);

        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(existingPolicy));

        // when
        DeliveryPolicyGetResponse response = deliveryPolicyService.update(req);

        assertThat(response.getFreeDeliveryThreshold()).isEqualTo(newFreeDeliveryThreshold);
        assertThat(response.getBaseDeliveryFee()).isEqualTo(newBaseDeliveryFee);

        assertThat(existingPolicy.getFreeDeliveryThreshold()).isEqualTo(newFreeDeliveryThreshold);
        assertThat(existingPolicy.getBaseDeliveryFee()).isEqualTo(newBaseDeliveryFee);
    }

    @Test
    @DisplayName("정책이 설정되지 않았을 경우 수정 시도 시 예외가 발생한다")
    void update_policyNotConfigured_throwsException() {
        // given
        DeliveryPolicyUpdateRequest req = new DeliveryPolicyUpdateRequest(50000, 2500);

        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryPolicyService.update(req))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ErrorCode.DELIVERY_POLICY_NOT_CONFIGURED.getMessage());
    }

    @Test
    @DisplayName("현재 배송 정책을 성공적으로 조회한다")
    void getDeliveryPolicy_success() {
        // given
        DeliveryPolicy existingPolicy = new DeliveryPolicy(freeDeliveryThreshold, baseDeliveryFee);

        // findTopByOrderByIdAsc 호출 시 기존 정책 객체 반환을 가정
        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(existingPolicy));

        // when
        DeliveryPolicyGetResponse response = deliveryPolicyService.getDeliveryPolicy();

        // then
        assertThat(response.getFreeDeliveryThreshold()).isEqualTo(freeDeliveryThreshold);
        assertThat(response.getBaseDeliveryFee()).isEqualTo(baseDeliveryFee);
    }

    @Test
    @DisplayName("정책이 설정되지 않았을 경우 조회 시도 시 예외가 발생한다")
    void getDeliveryPolicy_policyNotConfigured_throwsException() {
        // given
        // findTopByOrderByIdAsc 호출 시 정책이 없음을 가정
        when(deliveryPolicyRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryPolicyService.getDeliveryPolicy())
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ErrorCode.DELIVERY_POLICY_NOT_CONFIGURED.getMessage());
    }
}