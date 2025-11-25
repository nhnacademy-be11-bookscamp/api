package store.bookscamp.api.rank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.DELIVERED;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.GOLD;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.PLATINUM;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.STANDARD;
import static store.bookscamp.api.pointpolicy.entity.RewardType.RATE;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils; // [추가] 리플렉션 유틸
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
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

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("회원 등급 정보 조회 성공")
    void getMemberRank_success() {
        // given
        PointPolicy policy = pointPolicyRepository.save(new PointPolicy(
                GOLD,
                RATE,
                5
        ));

        Rank rank = new Rank(policy, "GOLD", 100000, 200000);
        rankRepository.save(rank);

        Member member = createMember("user1", rank);

        // when
        RankGetDto result = rankService.getMemberRank(member.getId());

        // then
        assertThat(result.name()).isEqualTo("GOLD");
        assertThat(result.value()).isEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 조회 시 MEMBER_NOT_FOUND 예외 발생")
    void getMemberRank_memberNotFound_fail() {
        // given
        Long nonExistentId = 9999L;

        // when, then
        assertThatThrownBy(() -> rankService.getMemberRank(nonExistentId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 실적에 따라 회원 등급이 일괄 업데이트된다")
    void updateAllMemberGrades_success() {
        // given
        PointPolicy standardPolicy = pointPolicyRepository.save(new PointPolicy(STANDARD, RATE, 1));
        Rank standardRank = rankRepository.save(new Rank(standardPolicy, "STANDARD", 0, 10000));

        PointPolicy platinumPolicy = pointPolicyRepository.save(new PointPolicy(PLATINUM, RATE, 10));
        Rank platinumRank = rankRepository.save(new Rank(platinumPolicy, "PLATINUM", 10001, 999999));

        Member member = createMember("buyer", standardRank);

        // 20000원 주문 생성 (PLATINUM 기준 충족)
        createOrder(member, 20000);

        em.flush();
        em.clear();

        // when
        rankService.updateAllMemberGrades();

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getRank().getName()).isEqualTo("PLATINUM");
    }

    @Test
    @DisplayName("주문 실적이 없으면 등급이 유지되거나 기본 등급으로 설정된다")
    void updateAllMemberGrades_noOrders_maintainRank() {
        // given
        PointPolicy policy = pointPolicyRepository.save(new PointPolicy(STANDARD, RATE, 1));
        Rank standardRank = rankRepository.save(new Rank(policy, "STANDARD", 0, 10000));

        Member member = createMember("ghostUser", standardRank);

        em.flush();
        em.clear();

        // when
        rankService.updateAllMemberGrades();

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getRank().getName()).isEqualTo("STANDARD");
    }

    private Member createMember(String username, Rank rank) {
        String uniqueVal = UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member(
                "test",
                "password",
                username + uniqueVal + "@test.com",
                "010-" + uniqueVal,
                0,
                rank,
                NORMAL,
                LocalDate.now(),
                username + uniqueVal,
                LocalDateTime.now(),
                LocalDate.of(2000, 1, 1)
        );
        return memberRepository.save(member);
    }

    private void createOrder(Member member, int netAmount) {
        OrderInfo order = new OrderInfo(
                UUID.randomUUID().toString(),
                member,
                null, null,
                netAmount,
                netAmount + 2500,
                2500, 0, 0,
                netAmount + 2500,
                DELIVERED,
                0
        );

        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());

        orderInfoRepository.save(order);
    }
}