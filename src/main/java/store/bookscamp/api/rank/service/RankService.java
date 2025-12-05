package store.bookscamp.api.rank.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;
import store.bookscamp.api.rank.service.dto.RankGetDto;

@Service
@RequiredArgsConstructor
public class RankService {

    private final MemberRepository memberRepository;
    private final RankRepository rankRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @Transactional(readOnly = true)
    public RankGetDto getMemberRank(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRank() == null || member.getRank().getPointPolicy() == null) {
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Long pointPolicyId = member.getRank().getPointPolicy().getId();
        PointPolicy pointPolicy = pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow();

        String name = member.getRank().getName();
        Integer value = pointPolicy.getRewardValue();

        return new RankGetDto(name, value);
    }

    @Retryable(noRetryFor = ApplicationException.class, backoff = @Backoff(multiplier = 2.0, maxDelay = 10000), listeners = "customRetryListener")
    @Transactional
    public void updateSingleMemberGrade(Member member, List<Rank> allRanks, int amount) {
        Rank targetRank = findMatchingRank(allRanks, amount);

        if (targetRank != null && !targetRank.equals(member.getRank())) {
            member.updateRank(targetRank);
            memberRepository.save(member);
        }
    }

    private Rank findMatchingRank(List<Rank> ranks, int amount) {
        return ranks.stream()
                .filter(rank -> rank.contains(amount))
                .findFirst()
                .orElseGet(() -> ranks.isEmpty() ? null : ranks.getFirst());
    }
}