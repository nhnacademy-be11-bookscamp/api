package store.bookscamp.api.rank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.*;
import static store.bookscamp.api.pointpolicy.entity.RewardType.RATE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;
import store.bookscamp.api.rank.service.dto.RankGetDto;

@SpringBootTest
@Transactional
class RankServiceTest {

    @Autowired
    private RankService rankService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("회원 등급 정보 조회 성공")
    void getMemberRank_success() {
        // given
        PointPolicy policy = pointPolicyRepository.save(new PointPolicy(GOLD, RATE, 5));
        Rank rank = rankRepository.save(new Rank(policy, "GOLD", 100000, 200000));
        Member member = createMember("user1", rank);

        // when
        RankGetDto result = rankService.getMemberRank(member.getId());

        // then
        assertThat(result.name()).isEqualTo("GOLD");
        assertThat(result.value()).isEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 조회 시 예외 발생")
    void getMemberRank_memberNotFound_fail() {
        // given
        Long nonExistentId = 9999L;

        // when, then
        assertThatThrownBy(() -> rankService.getMemberRank(nonExistentId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("단일 회원 등급 업데이트 - 실적 충족 시 등급 상승")
    void updateSingleMemberGrade_upgrade() {
        // given
        // 1. 등급 정책 설정 (STANDARD, PLATINUM)
        Rank standardRank = createRank(STANDARD, "STANDARD", 0, 10000);
        Rank platinumRank = createRank(PLATINUM, "PLATINUM", 10001, 999999);
        List<Rank> allRanks = rankRepository.findAll();

        // 2. STANDARD 등급 회원 생성
        Member member = createMember("buyer", standardRank);

        // when
        // 20000원 실적이 있다고 가정하고 서비스 호출
        rankService.updateSingleMemberGrade(member, allRanks, 20000);

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getRank().getName()).isEqualTo("PLATINUM");
    }

    @Test
    @DisplayName("단일 회원 등급 업데이트 - 실적 부족 시 등급 유지")
    void updateSingleMemberGrade_maintain() {
        // given
        Rank standardRank = createRank(STANDARD, "STANDARD", 0, 10000);
        Rank platinumRank = createRank(PLATINUM, "PLATINUM", 10001, 999999);
        List<Rank> allRanks = rankRepository.findAll();

        Member member = createMember("ghostUser", standardRank);

        // when
        // 실적이 0원이라고 가정
        rankService.updateSingleMemberGrade(member, allRanks, 0);

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getRank().getName()).isEqualTo("STANDARD");
    }

    private Rank createRank(store.bookscamp.api.pointpolicy.entity.PointPolicyType type, String name, int min, int max) {
        PointPolicy policy = pointPolicyRepository.save(new PointPolicy(type, RATE, 1));
        return rankRepository.save(new Rank(policy, name, min, max));
    }

    private Member createMember(String username, Rank rank) {
        String uniqueVal = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(new Member(
                "test", "password", username + uniqueVal + "@test.com", "010-" + uniqueVal,
                0, rank, NORMAL, LocalDate.now(), username + uniqueVal,
                LocalDateTime.now(), LocalDate.of(2000, 1, 1)
        ));
    }
}