package store.bookscamp.api.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.Status;
import store.bookcamp.api.member.repository.MemberRepository;
import store.bookscamp.api.member.service.MemberCreateDto;
import store.bookscamp.api.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberCreateDto createDto;
    private Member savedMember;
    private String expectedName = "홍길동";

    @BeforeEach
    void setUp() {
        createDto = new MemberCreateDto(
                "testId",
                "testPassword123!",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1)
        );
        savedMember = new Member(
                1L,
                createDto.accountId(),
                createDto.password(),
                createDto.name(),
                createDto.email(),
                createDto.phone(),
                0,
                Status.NORMAL,
                LocalDate.now(),
                null,
                createDto.birthDate()
        );
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
    @DisplayName("회원 생성 시 Entity 필드 매핑 검증 (ArgumentCaptor)")
    void createMember_entityMappingVerification() {

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        final LocalDate expectedStatusUpdateDate = LocalDate.now();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        memberService.create(createDto);

        verify(memberRepository, times(1)).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();

        assertNotNull(capturedMember, "캡처된 Member 객체는 null이 아니어야 합니다.");

        assertEquals(createDto.accountId(), capturedMember.getAccountId(), "accountId 불일치");
        assertEquals(createDto.password(), capturedMember.getPassword(), "password 불일치");
        assertEquals(createDto.name(), capturedMember.getName(), "name 불일치");
        assertEquals(createDto.email(), capturedMember.getEmail(), "email 불일치");
        assertEquals(createDto.phone(), capturedMember.getPhone(), "phone 불일치");
        assertEquals(createDto.birthDate(), capturedMember.getBirthDate(), "birthDate 불일치");

        assertEquals(0, capturedMember.getPoint(), "초기 point는 0이어야 합니다.");
        assertEquals(Status.NORMAL, capturedMember.getStatus(), "초기 Status는 NORMAL이어야 합니다.");

        assertEquals(expectedStatusUpdateDate, capturedMember.getStatusUpdateDate(), "StatusUpdateDate 불일치");
        assertNull(capturedMember.getLastLoginAt(), "최종 로그인 시각은 null이어야 합니다.");
        assertNull(capturedMember.getId(), "ID는 null이어야 합니다.");
    }
}