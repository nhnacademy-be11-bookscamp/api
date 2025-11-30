package store.bookscamp.api.rank.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.rank.service.dto.RankSummaryDto;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;
import store.bookscamp.api.rank.service.RankService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankScheduler {

    private final RankService rankService;
    private final MemberRepository memberRepository;
    private final RankRepository rankRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyRankUpdate() {
        log.info("일일 회원 등급 업데이트 시작");

        List<Rank> allRanks = rankRepository.findAll();

        Map<Long, Integer> memberAmountMap = rankRepository.getMemberNetTotalForGrading().stream()
                .collect(Collectors.toMap(
                        RankSummaryDto::memberId,
                        RankSummaryDto::totalNetAmount
                ));

        List<Member> members = memberRepository.findAll();

        int successCount = 0;
        int failCount = 0;

        for (Member member : members) {
            try {
                int amount = memberAmountMap.getOrDefault(member.getId(), 0);

                rankService.updateSingleMemberGrade(member, allRanks, amount);
                successCount++;

            } catch (Exception e) {
                failCount++;
                log.error("회원 등급 업데이트 실패 - memberId: {}, cause: {}", member.getId(), e.getMessage());
            }
        }

        log.info("일일 회원 등급 업데이트 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}