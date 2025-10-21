package store.bookcamp.api.member.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.response.MemberCreateResponse;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.State;
import store.bookcamp.api.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberCreateRequest createRequest;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        createRequest = new MemberCreateRequest(
                "testId",
                "testPassword123!",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1)
        );

        savedMember = new Member();
        savedMember.setId(1L);
        savedMember.setAccountId(createRequest.id());
        savedMember.setPassword(createRequest.password());
        savedMember.setName(createRequest.name());
        savedMember.setEmail(createRequest.email());
        savedMember.setPhone(createRequest.phone());
        savedMember.setBirth(createRequest.birth());
        savedMember.setPoint(0);
        savedMember.setState(State.NORMAL);
        savedMember.setStatusUpdateDate(LocalDate.now());
    }

    @Test
    @DisplayName("회원 생성 성공 테스트")
    void createMember_success() {
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        MemberCreateResponse actualResponse = memberService.create(createRequest);
        verify(memberRepository, times(1)).save(any(Member.class));
        assertNotNull(actualResponse, "응답 객체는 null이 아니어야 합니다.");
        assertEquals(savedMember.getName(), actualResponse.name(), "응답 이름은 저장된 회원의 이름과 일치해야 합니다.");
    }

    @Test
    @DisplayName("회원 생성 시 Entity 필드 매핑 검증")
    void createMember_entityMappingVerification() {
        var memberCaptor = argThat((Member argument) -> {
            assertEquals(createRequest.id(), argument.getAccountId());
            assertEquals(createRequest.password(), argument.getPassword());
            assertEquals(createRequest.name(), argument.getName());
            assertEquals(0, argument.getPoint());
            assertEquals(State.NORMAL, argument.getState());
            assertTrue(argument.getStatusUpdateDate().isEqual(LocalDate.now()) || argument.getStatusUpdateDate().isEqual(LocalDate.now().minusDays(1)));
            assertNull(argument.getLastLoginAt());
            return true;
        });
        when(memberRepository.save(memberCaptor)).thenReturn(savedMember);
        memberService.create(createRequest);
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}