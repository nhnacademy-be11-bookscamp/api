package store.bookscamp.api.rank.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.DELIVERED;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.PENDING;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.SHIPPING;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import store.bookscamp.api.common.config.JpaConfig;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.rank.repository.custom.impl.RankRepositoryCustomImpl;
import store.bookscamp.api.rank.service.dto.RankSummaryDto;

@DataJpaTest
@Import(JpaConfig.class)
class RankRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private RankRepositoryCustomImpl rankRepository;

    @Test
    @DisplayName("등급 산정 - 최근 3개월 주문의 순수 금액 합산 검증")
    void getMemberNetTotalForGrading_Success() {
        // Given
        Member member = createMember("user1", "user1@test.com", "010-1111-1111");

        createOrder(member, 10000, DELIVERED, LocalDateTime.now().minusMonths(1));
        createOrder(member, 20000, SHIPPING, LocalDateTime.now().minusMonths(2));
        createOrder(member, 5000, PENDING, LocalDateTime.now().minusDays(1));

        // When
        List<RankSummaryDto> result = rankRepository.getMemberNetTotalForGrading();

        // Then
        assertThat(result).hasSize(1);

        RankSummaryDto summary = result.getFirst();
        assertThat(summary.memberId()).isEqualTo(member.getId());
        assertThat(summary.totalNetAmount()).isEqualTo(35000);
    }

    @Test
    @DisplayName("등급 산정 - 3개월 이전 데이터와 삭제된 데이터는 합계에서 제외")
    void getMemberNetTotalForGrading_ExcludeFilters() {
        // Given
        Member member = createMember("user2", "user2@test.com", "010-2222-2222");

        createOrder(member, 10000, DELIVERED, LocalDateTime.now().minusMonths(3).plusDays(1));
        createOrder(member, 50000, DELIVERED, LocalDateTime.now().minusMonths(3).minusDays(1));
        createDeletedOrder(member);

        // When
        List<RankSummaryDto> result = rankRepository.getMemberNetTotalForGrading();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalNetAmount()).isEqualTo(10000);
    }

    private Member createMember(String username, String email, String phone) {
        Member member = new Member(
                "test",
                "test123",
                email,
                phone,
                0,
                null,
                NORMAL,
                LocalDate.now(),
                username,
                LocalDateTime.now(),
                LocalDate.of(2000, 1, 1)
        );
        em.persist(member);
        return member;
    }

    private void createOrder(Member member, int netAmount, OrderStatus status, LocalDateTime createdAt) {
        OrderInfo order = new OrderInfo(
                UUID.randomUUID().toString(),
                member,
                null,
                null,
                netAmount,
                netAmount + 2500,
                2500,
                0,
                0,
                netAmount + 2500,
                status,
                0
        );
        em.persist(order);

        em.createQuery("UPDATE OrderInfo o SET o.createdAt = :date WHERE o.id = :id")
                .setParameter("date", createdAt)
                .setParameter("id", order.getId())
                .executeUpdate();

        em.clear();
    }

    private void createDeletedOrder(Member member) {
        OrderInfo order = new OrderInfo(
                UUID.randomUUID().toString(),
                member,
                null, null,
                20000, 20000, 0, 0, 0, 20000,
                DELIVERED, 0
        );
        em.persist(order);

        em.createQuery("UPDATE OrderInfo o SET o.deletedAt = NOW() WHERE o.id = :id")
                .setParameter("id", order.getId())
                .executeUpdate();

        em.clear();
    }
}