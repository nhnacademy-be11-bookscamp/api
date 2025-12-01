package store.bookscamp.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import store.bookscamp.api.member.service.MemberService;
import store.bookscamp.api.member.service.dto.MemberGetDto;
import store.bookscamp.api.member.service.dto.MemberPageDto;
import store.bookscamp.api.member.service.dto.MemberStatusUpdateDto;
import store.bookscamp.api.member.service.dto.MemberUpdateDto;
import store.bookscamp.api.member.entity.MemberStatus;

import store.bookscamp.api.member.controller.request.MemberCreateRequest;
import store.bookscamp.api.member.controller.request.MemberStatusUpdateRequest;
import store.bookscamp.api.member.controller.request.MemberUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private static final Long USER_ID = 1L;
    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    @Test
    @DisplayName("GET /member - 회원 조회 성공 (USER 권한)")
    void getMember() throws Exception {

        MemberGetDto mockDto = new MemberGetDto(
                "testUser",
                "홍길동",
                "test@email.com",
                "010-1234-5678",
                100,
                LocalDate.of(1990, 1, 1)
        );

        given(memberService.getMember(USER_ID)).willReturn(mockDto);

        mockMvc.perform(get("/member")
                        .header(HEADER_USER_ID, String.valueOf(USER_ID))
                        .header(HEADER_USER_ROLE, ROLE_USER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.point").value(100));
    }

    @Test
    @DisplayName("GET /member/check-id - 아이디 중복 발생 (409 Conflict)")
    void checkIdDuplicate_True() throws Exception {
        String duplicateId = "duplicateId";
        given(memberService.checkIdDuplicate(duplicateId)).willReturn(true);

        mockMvc.perform(get("/member/check-id")
                        .param("id", duplicateId))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("GET /member/check-id - 아이디 사용 가능 (200 OK)")
    void checkIdDuplicate_False() throws Exception {
        String availableId = "newId";
        given(memberService.checkIdDuplicate(availableId)).willReturn(false);

        mockMvc.perform(get("/member/check-id")
                        .param("id", availableId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 아이디입니다."));
    }

    @Test
    @DisplayName("POST /member/sign-up - 회원가입 성공")
    void createMember() throws Exception {

        MemberCreateRequest request = new MemberCreateRequest(
                "newUser",
                "password123!",
                "신규회원",
                "new@email.com",
                "010-9876-5432",
                LocalDate.now()
        );

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).checkEmailPhoneDuplicate(request.email(), request.phone());
        verify(memberService).createMember(any());
    }

    @Test
    @DisplayName("PUT /member - 회원정보 수정 성공 (USER 권한)")
    void updateMember() throws Exception {

        MemberUpdateRequest request = new MemberUpdateRequest(
                "변경이름",
                "update@email.com",
                "010-1111-2222"
        );

        mockMvc.perform(put("/member")
                        .header(HEADER_USER_ID, String.valueOf(USER_ID))
                        .header(HEADER_USER_ROLE, ROLE_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).checkEmailPhoneDuplicateForUpdate(eq(USER_ID), eq(request.email()), eq(request.phone()));
        verify(memberService).updateMember(eq(USER_ID), any(MemberUpdateDto.class));
    }

    @Test
    @DisplayName("DELETE / - 회원탈퇴 성공 (USER 권한)")
    void deleteMember() throws Exception {
        mockMvc.perform(delete("/member")
                        .header(HEADER_USER_ID, String.valueOf(USER_ID))
                        .header(HEADER_USER_ROLE, ROLE_USER))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).deleteMember(USER_ID);
    }

    @Test
    @DisplayName("GET /admin/member - 전체 회원 조회 (ADMIN 권한)")
    void getAllMembers() throws Exception {
        MemberPageDto pageDto = new MemberPageDto(
                1L,
                "adminUser",
                "관리자",
                "admin@email.com",
                "010-0000-0000",
                MemberStatus.NORMAL,
                LocalDateTime.now(),
                LocalDate.now()
        );

        Page<MemberPageDto> page = new PageImpl<>(List.of(pageDto));
        given(memberService.getAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/admin/member")
                        .header(HEADER_USER_ROLE, ROLE_ADMIN)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("adminUser"))
                .andExpect(jsonPath("$.content[0].status").value("NORMAL"));
    }

    @Test
    @DisplayName("PUT /admin/member/updateStatus - 회원 상태 변경 (ADMIN 권한)")
    void updateMemberStatus() throws Exception {

        MemberStatusUpdateRequest request = new MemberStatusUpdateRequest(2L, "BLOCK");

        mockMvc.perform(put("/admin/member/updateStatus")
                        .header(HEADER_USER_ROLE, ROLE_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).updateMemberState(any(MemberStatusUpdateDto.class));
    }
}