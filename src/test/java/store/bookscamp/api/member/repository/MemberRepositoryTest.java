package store.bookscamp.api.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import store.bookscamp.api.member.entity.Member;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("특정 월의 생일을 가진 회원만 조회된다")
    void findAllByBirthDateMonth_success() {
        // given
        Member member1 = memberRepository.save(new Member(
                "회원",
                "1234",
                "member1@naver.com",
                "01000000001",
                0,
                NORMAL,
                LocalDate.now(),
                "member1",
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 1)
        ));

        memberRepository.save(new Member(
                "회원2",
                "1234",
                "member2@naver.com",
                "01000000002",
                0,
                NORMAL,
                LocalDate.now(),
                "member2",
                LocalDateTime.now(),
                LocalDate.of(2001, 10, 10)
        ));

        Member member3 = memberRepository.save(new Member(
                "회원3",
                "1234",
                "member3@naver.com",
                "01000000003",
                0,
                NORMAL,
                LocalDate.now(),
                "member3",
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 10)
        ));

        int targetMonth = 1;

        // when
        List<Member> result = memberRepository.findAllByBirthDateMonth(targetMonth);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(Member::getName)
                .containsExactlyInAnyOrder(member1.getName(), member3.getName());
    }
}