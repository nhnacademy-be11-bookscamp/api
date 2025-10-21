package store.bookcamp.api.member.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.Status;
import store.bookcamp.api.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberDto createDto;
    private Member savedMember;
    private String expectedName = "홍길동";

    @BeforeEach
    void setUp() {
        createDto = new MemberDto(
                "testId",
                "testPassword123!",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1)
        );
        savedMember = new Member(createDto);
    }

    @Test
    @DisplayName("회원 생성 성공 테스트")
    void createMember_success() {
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        String actualName = memberService.create(createDto);

        verify(memberRepository, times(1)).save(any(Member.class));
        assertNotNull(actualName, "응답 이름은 null이 아니어야 합니다.");
        assertEquals(expectedName, actualName, "응답 이름은 저장된 회원의 이름과 일치해야 합니다.");
    }

    @Test
    @DisplayName("회원 생성 시 Entity 필드 매핑 검증")
    void createMember_entityMappingVerification() {
        var memberCaptor = argThat((Member argument) -> {
            assertEquals(createDto.accountId(), argument.getAccountId());
            assertEquals(createDto.password(), argument.getPassword());
            assertEquals(createDto.name(), argument.getName());
            assertEquals(0, argument.getPoint());
            assertEquals(Status.NORMAL, argument.getStatus());
            assertTrue(argument.getStatusUpdateDate().isEqual(LocalDate.now()) || argument.getStatusUpdateDate().isEqual(LocalDate.now().minusDays(1)));
            assertNull(argument.getLastLoginAt());
            return true;
        });

        when(memberRepository.save(memberCaptor)).thenReturn(savedMember);

        memberService.create(createDto);

        verify(memberRepository, times(1)).save(any(Member.class));
    }
}