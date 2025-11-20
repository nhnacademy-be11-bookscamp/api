package store.bookscamp.api.rank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import store.bookscamp.api.rank.service.dto.RankSummaryDto;

@Service
@RequiredArgsConstructor
public class RankService {

    private final MemberRepository memberRepository;
    private final RankRepository rankRepository;
    private final PointPolicyRepository pointPolicyRepository;

    public RankGetDto getMemberRank(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        Long pointPolicyId = member.getRank().getPointPolicy().getId();
        PointPolicy pointPolicy = pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow();

        String name = member.getRank().getName();
        Integer value = pointPolicy.getRewardValue();

        return new RankGetDto(name, value);
    }

    @Transactional
    public void updateAllMemberGrades() {

        List<Rank> allRanks = rankRepository.findAll();

        Map<Long, BigDecimal> memberAmountMap = rankRepository.getMemberNetTotalForGrading().stream()
                .collect(Collectors.toMap(
                        RankSummaryDto::memberId,
                        RankSummaryDto::totalNetAmount
                ));

        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            BigDecimal decimalAmount = memberAmountMap.getOrDefault(member.getId(), BigDecimal.ZERO);

            int amount = decimalAmount.intValue();

            Rank targetRank = findMatchingRank(allRanks, amount);

            if (targetRank != null && !targetRank.equals(member.getRank())) {
                member.updateRank(targetRank);
            }
        }
    }

    private Rank findMatchingRank(List<Rank> ranks, int amount) {
        return ranks.stream()
                .filter(rank -> rank.contains(amount))
                .findFirst()
                .orElseGet(() -> ranks.isEmpty() ? null : ranks.getFirst());
    }
}
