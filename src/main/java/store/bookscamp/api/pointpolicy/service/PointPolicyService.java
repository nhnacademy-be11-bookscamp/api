package store.bookscamp.api.pointpolicy.service;

import static store.bookscamp.api.common.exception.ErrorCode.POINT_POLICY_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyCreateDto;
import store.bookscamp.api.pointpolicy.service.dto.PointPolicyUpdateDto;

@Service
@RequiredArgsConstructor
public class PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;

    public Long createPointPolicy(PointPolicyCreateDto dto) {
        PointPolicy pointPolicy = new PointPolicy(dto.pointPolicyType(), dto.rewardType(), dto.rewardValue());
        return pointPolicyRepository.save(pointPolicy)
                .getId();
    }

    @Transactional
    public void updatePointPolicy(PointPolicyUpdateDto dto) {
        PointPolicy pointPolicy = pointPolicyRepository.findById(dto.pointPolicyId())
                .orElseThrow(() -> new ApplicationException(POINT_POLICY_NOT_FOUND));
        pointPolicy.updatePointPolicy(dto.pointPolicyType(), dto.rewardType(), dto.rewardValue());
    }

    public void deletePointPolicy(Long pointPolicyId) {
        PointPolicy pointPolicy = pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow(() -> new ApplicationException(POINT_POLICY_NOT_FOUND));
        pointPolicyRepository.delete(pointPolicy);
    }

    public PointPolicy getPointPolicy(Long pointPolicyId) {
        return pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow(() -> new ApplicationException(POINT_POLICY_NOT_FOUND));
    }

    public List<PointPolicy> listPointPolicies() {
        return pointPolicyRepository.findAll();
    }
}
