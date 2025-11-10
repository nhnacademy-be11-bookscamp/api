package store.bookscamp.api.deliverypolicy.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyCreateRequest;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.controller.response.DeliveryPolicyGetResponse;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class DeliveryPolicyService {

    private DeliveryPolicyRepository deliveryPolicyRepository;

    public DeliveryPolicyGetResponse create(DeliveryPolicyCreateRequest request) {
        DeliveryPolicy saved = deliveryPolicyRepository.save(request.toEntity());
        return DeliveryPolicyGetResponse.fromEntity(saved);

    }

    public DeliveryPolicyGetResponse update(Long id, DeliveryPolicyUpdateRequest req) {
        DeliveryPolicy policy = deliveryPolicyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DeliveryPolicy not found: " + id));

        if (req.hasFreeThreshold()) {
            policy.update(req.getFreeDeliveryThreshold(),
                    req.hasBaseFee() ? req.getBaseDeliveryFee() : policy.getBaseDeliveryFee());
        }
        if (req.hasBaseFee() && !req.hasFreeThreshold()) {
            policy.update(policy.getFreeDeliveryThreshold(), req.getBaseDeliveryFee());
        }
        return DeliveryPolicyGetResponse.fromEntity(policy);
    }

    /** 현재 정책 조회(사용자/관리자) */
    @Transactional(readOnly = true)
    public DeliveryPolicyGetResponse getCurrent() {
        DeliveryPolicy policy = deliveryPolicyRepository.findCurrent()
                .orElseThrow(() -> new EntityNotFoundException("DeliveryPolicy is not configured."));
        return DeliveryPolicyGetResponse.fromEntity(policy);
    }

    /** 총액 기준 무료배송 여부(사용자) */
    @Transactional(readOnly = true)
    public boolean isFreeByTotal(int orderTotal) {
        DeliveryPolicy p = deliveryPolicyRepository.findCurrent()
                .orElseThrow(() -> new EntityNotFoundException("DeliveryPolicy is not configured."));
        return orderTotal >= p.getFreeDeliveryThreshold();
    }

    /** 총액 기준 배송비 계산(사용자) */
    @Transactional(readOnly = true)
    public int calculateFee(int orderTotal) {
        return isFreeByTotal(orderTotal) ? 0 :
                deliveryPolicyRepository.findCurrent()
                        .orElseThrow(() -> new EntityNotFoundException("DeliveryPolicy is not configured."))
                        .getBaseDeliveryFee();
    }




}
