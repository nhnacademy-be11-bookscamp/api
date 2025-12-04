package store.bookscamp.api.rank.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;
import store.bookscamp.api.rank.service.RankService;
import store.bookscamp.api.rank.service.dto.RankSummaryDto;

@ExtendWith(MockitoExtension.class)
class RankSchedulerTest {

    @InjectMocks
    private RankScheduler rankScheduler;

    @Mock
    private RankService rankService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RankRepository rankRepository;

    @Test
    @DisplayName("스케줄러 실행 시 회원 등급 업데이트 로직이 정상 호출된다")
    void dailyRankUpdate_success() {
        // given
        // 1. Rank 목록 Mock (기본 등급 생성)
        Rank mockRank = new Rank(null, "STANDARD", 0, 10000);
        List<Rank> ranks = List.of(mockRank);
        given(rankRepository.findAll()).willReturn(ranks);

        // 2. 회원 실적 통계 Mock (memberId: 1L인 회원이 50,000원을 썼다고 가정)
        RankSummaryDto summaryDto = new RankSummaryDto(1L, 50000);
        given(rankRepository.getMemberNetTotalForGrading()).willReturn(List.of(summaryDto));

        // 3. 회원 목록 Mock (생성자 사용)
        Member mockMember = new Member(
                "테스트멤버", "password", "test@test.com", "010-1234-5678",
                0, mockRank, NORMAL, LocalDate.now(), "testuser",
                LocalDateTime.now(), LocalDate.of(2000, 1, 1)
        );

        // 중요: 생성자로는 ID를 설정할 수 없으므로 ReflectionTestUtils로 ID(1L) 주입
        ReflectionTestUtils.setField(mockMember, "id", 1L);

        given(memberRepository.findAll()).willReturn(List.of(mockMember));

        // when
        rankScheduler.dailyRankUpdate();

        // then
        // 스케줄러가 통계 정보(50,000원)를 잘 매핑해서 서비스 메서드를 호출했는지 검증
        verify(rankService, times(1)).updateSingleMemberGrade(
                eq(mockMember),
                eq(ranks),
                eq(50000) // memberId 1L에 매핑된 금액
        );
    }
}