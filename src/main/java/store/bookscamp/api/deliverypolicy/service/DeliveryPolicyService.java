package store.bookscamp.api.deliverypolicy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryPolicyService {

    private final DeliveryPolicyRepository deliveryPolicyRepository;

    @Transactional
    public DeliveryPolicyGetResponse update(DeliveryPolicyUpdateRequest req) {
        DeliveryPolicy policy = deliveryPolicyRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ApplicationException(ErrorCode.DELIVERY_POLICY_NOT_CONFIGURED));

        policy.update(req.getFreeDeliveryThreshold(), req.getBaseDeliveryFee());
        return DeliveryPolicyGetResponse.fromEntity(policy);
    }

    /** 현재 정책 조회(사용자/관리자) */
    public DeliveryPolicyGetResponse getDeliveryPolicy() {
        DeliveryPolicy policy = deliveryPolicyRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ApplicationException(ErrorCode.DELIVERY_POLICY_NOT_CONFIGURED));
        return DeliveryPolicyGetResponse.fromEntity(policy);
    }
}
