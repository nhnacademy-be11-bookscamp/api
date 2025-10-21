package store.bookcamp.api.member.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.response.MemberCreateResponse;
import store.bookcamp.api.member.service.MemberDto; // MemberDto 임포트 추가
import store.bookcamp.api.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MemberCreateRequest mockRequest;
    private MemberCreateResponse mockResponse;
    private String expectedName = "홍길동";

    @BeforeEach
    void setUp() {
        mockRequest = new MemberCreateRequest(
                "testId",
                "testPassword123",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1)
        );
        mockResponse = new MemberCreateResponse(expectedName);
    }

    @Test
    @DisplayName("회원 생성 API 호출 성공 및 201 Created 반환 테스트")
    void createMember_success_returns201() {
        when(memberService.create(any(MemberDto.class))).thenReturn(expectedName);

        ResponseEntity<MemberCreateResponse> actualResponse = memberController.createMember(mockRequest);

        verify(memberService, times(1)).create(any(MemberDto.class));

        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode(), "HTTP 상태 코드는 201 Created여야 합니다.");

        assertNotNull(actualResponse.getBody(), "응답 본문은 null이 아니어야 합니다.");
        assertEquals(expectedName, actualResponse.getBody().name(), "응답 DTO의 name 필드가 일치해야 합니다.");
    }
}