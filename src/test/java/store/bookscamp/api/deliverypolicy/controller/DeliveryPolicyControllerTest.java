package store.bookscamp.api.deliverypolicy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.deliverypolicy.service.DeliveryPolicyService;

@SpringBootTest
@Transactional
class DeliveryPolicyControllerTest {

    @Autowired
    private DeliveryPolicyService deliveryPolicyService;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    @BeforeEach
    void setUp() {
        deliveryPolicyRepository.deleteAll();
    }

    @Test
    @DisplayName("getDeliveryPolicy — 가장 오래된 정책을 조회하여 DTO로 반환한다")
    void getDeliveryPolicy_success() {
        DeliveryPolicy p1 = new DeliveryPolicy(0, 3000);
        DeliveryPolicy p2 = new DeliveryPolicy(30000, 0);
        deliveryPolicyRepository.save(p1);
        deliveryPolicyRepository.save(p2);

        DeliveryPolicyGetResponse res = deliveryPolicyService.getDeliveryPolicy();

        assertThat(res.getFreeDeliveryThreshold()).isEqualTo(0);
        assertThat(res.getBaseDeliveryFee()).isEqualTo(3000);
    }


    @Test
    @DisplayName("update — 가장 오래된 정책을 수정하고 DTO로 반환한다")
    void update_success() {
        // given
        DeliveryPolicy p1 = new DeliveryPolicy(0, 3000);
        deliveryPolicyRepository.save(p1);

        DeliveryPolicyUpdateRequest req = new DeliveryPolicyUpdateRequest();
        req.setFreeDeliveryThreshold(30000);
        req.setBaseDeliveryFee(2500);

        // when
        DeliveryPolicyGetResponse res = deliveryPolicyService.update(req);

        // then
        assertThat(res.getFreeDeliveryThreshold()).isEqualTo(30000);
        assertThat(res.getBaseDeliveryFee()).isEqualTo(2500);

        DeliveryPolicy updated = deliveryPolicyRepository.findById(p1.getId()).orElseThrow();
        assertThat(updated.getFreeDeliveryThreshold()).isEqualTo(30000);
        assertThat(updated.getBaseDeliveryFee()).isEqualTo(2500);
    }

    @Test
    @DisplayName("update — 정책이 하나도 없으면 DELIVERY_POLICY_NOT_CONFIGURED 예외를 던진다")
    void update_notConfigured() {
        // given
        DeliveryPolicyUpdateRequest req = new DeliveryPolicyUpdateRequest();
        req.setFreeDeliveryThreshold(30000);
        req.setBaseDeliveryFee(2500);

        // when & then
        assertThatThrownBy(() -> deliveryPolicyService.update(req))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_POLICY_NOT_CONFIGURED);
    }
}
