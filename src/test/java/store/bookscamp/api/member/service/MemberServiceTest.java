package store.bookscamp.api.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.publisher.MemberEventPublisher;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.member.service.dto.*;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberEventPublisher memberEventPublisher;

    @Mock
    private RankRepository rankRepository;

    private Member createMember(Long id) {
        Member member = mock(Member.class);

        lenient().when(member.getId()).thenReturn(id);
        lenient().when(member.getUsername()).thenReturn("testUser");
        lenient().when(member.getName()).thenReturn("홍길동");
        lenient().when(member.getEmail()).thenReturn("test@email.com");
        lenient().when(member.getPhone()).thenReturn("010-1234-5678");
        lenient().when(member.getStatus()).thenReturn(MemberStatus.NORMAL);
        lenient().when(member.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        return member;
    }

    @Nested
    @DisplayName("회원 조회 (getMember)")
    class GetMemberTest {
        @Test
        @DisplayName("성공: 존재하는 ID 조회 시 DTO 반환")
        void getMember_Success() {

            Long memberId = 1L;
            Member member = createMember(memberId);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));


            MemberGetDto result = memberService.getMember(memberId);


            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(member.getUsername());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID 조회 시 예외 발생 (MEMBER_NOT_FOUND)")
        void getMember_Fail_NotFound() {

            Long memberId = 999L;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());


            assertThatThrownBy(() -> memberService.getMember(memberId))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("중복 검사 로직")
    class DuplicateCheckTest {

        @Test
        @DisplayName("아이디 중복 검사: 중복이면 카운트 > 0")
        void checkIdDuplicate_True() {
            given(memberRepository.existsByUsername("duplicateId")).willReturn(1L);

            assertThat(memberService.checkIdDuplicate("duplicateId")).isGreaterThan(0L);
        }

        @Test
        @DisplayName("가입 시 이메일 중복 발생 -> 예외 (EMAIL_DUPLICATE)")
        void checkEmailPhoneDuplicate_EmailFail() {
            String email = "dup@email.com";
            String phone = "010-0000-0000";
            given(memberRepository.existsByEmail(email)).willReturn(true);

            assertThatThrownBy(() -> memberService.checkEmailPhoneDuplicate(email, phone))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATE);
            verify(memberRepository, never()).existsByPhone(anyString());
        }

        @Test
        @DisplayName("가입 시 전화번호 중복 발생 -> 예외 (PHONE_DUPLICATE)")
        void checkEmailPhoneDuplicate_PhoneFail() {
            String email = "new@email.com";
            String phone = "010-1111-1111";
            given(memberRepository.existsByEmail(email)).willReturn(false);
            given(memberRepository.existsByPhone(phone)).willReturn(true);

            assertThatThrownBy(() -> memberService.checkEmailPhoneDuplicate(email, phone))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHONE_DUPLICATE);
        }

        @Test
        @DisplayName("수정 시 이메일 중복 (본인 제외 타인과 중복) -> 예외")
        void checkEmailPhoneDuplicateForUpdate_EmailFail() {
            Long myId = 1L;
            String targetEmail = "exist@email.com";
            given(memberRepository.existsByEmailAndIdNot(targetEmail, myId)).willReturn(true);

            assertThatThrownBy(() -> memberService.checkEmailPhoneDuplicateForUpdate(myId, targetEmail, "010-0000-0000"))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("회원 생성 (createMember)")
    class CreateMemberTest {
        @Test
        @DisplayName("성공: 등급 조회 성공 및 이벤트 발행 확인")
        void createMember_Success() {
            MemberCreateDto dto = new MemberCreateDto(
                    "user", "pw", "name", "e@mail.com", "phone", LocalDate.now()
            );
            Rank mockRank = mock(Rank.class);
            Member savedMember = mock(Member.class);

            given(rankRepository.findByPointPolicy_PointPolicyType(PointPolicyType.STANDARD))
                    .willReturn(Optional.of(mockRank));

            given(memberRepository.save(any(Member.class))).willReturn(savedMember);
            given(savedMember.getId()).willReturn(100L);

            memberService.createMember(dto);

            verify(memberRepository).save(any(Member.class));
            verify(memberEventPublisher).publishSignupEvent(100L);
        }

        @Test
        @DisplayName("실패: 기본 등급(STANDARD) 정책을 찾을 수 없음 (RANK_NOT_FOUND)")
        void createMember_Fail_RankNotFound() {
            MemberCreateDto dto = new MemberCreateDto("u", "p", "n", "e", "p", LocalDate.now());
            given(rankRepository.findByPointPolicy_PointPolicyType(PointPolicyType.STANDARD))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.createMember(dto))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 (updateMember)")
    class UpdateMemberTest {
        @Test
        @DisplayName("성공: 회원 정보를 업데이트함")
        void updateMember_Success() {
            Long id = 1L;
            MemberUpdateDto updateDto = new MemberUpdateDto("newName", "new@mail.com", "newPhone");
            Member member = mock(Member.class);

            given(memberRepository.findById(id)).willReturn(Optional.of(member));

            memberService.updateMember(id, updateDto);

            verify(member).changeInfo(updateDto.name(), updateDto.email(), updateDto.phone());
        }

        @Test
        @DisplayName("실패: 회원을 찾을 수 없음")
        void updateMember_Fail_NotFound() {

            given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.updateMember(1L, new MemberUpdateDto("n","e","p")))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 (deleteMember)")
    class DeleteMemberTest {
        @Test
        @DisplayName("성공: 회원 삭제 메서드 호출")
        void deleteMember_Success() {
            Long id = 1L;
            Member member = mock(Member.class);

            given(memberRepository.findById(id)).willReturn(Optional.of(member));

            memberService.deleteMember(id);

            verify(memberRepository).delete(member);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 회원")
        void deleteMember_Fail() {
            given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.deleteMember(1L))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("전체 회원 조회 (getAll)")
    class GetAllTest {
        @Test
        @DisplayName("성공: 페이지네이션 적용하여 DTO 변환 반환")
        void getAll_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Member member = createMember(1L);
            Page<Member> memberPage = new PageImpl<>(List.of(member));

            given(memberRepository.findAll(pageable)).willReturn(memberPage);

            Page<MemberPageDto> result = memberService.getAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).username()).isEqualTo("testUser");
            assertThat(result.getContent().get(0).status()).isEqualTo(MemberStatus.NORMAL);
        }
    }

    @Nested
    @DisplayName("회원 상태 변경 (updateMemberState)")
    class UpdateMemberStateTest {
        @Test
        @DisplayName("성공: 상태 변경 및 저장")
        void updateMemberState_Success() {
            Long id = 1L;
            MemberStatusUpdateDto dto = new MemberStatusUpdateDto(id, MemberStatus.WITHDRAWN);
            Member member = mock(Member.class);

            given(memberRepository.findById(id)).willReturn(Optional.of(member));

            memberService.updateMemberState(dto);

            verify(member).updateStatus(MemberStatus.WITHDRAWN);
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("실패: 회원을 찾을 수 없음")
        void updateMemberState_Fail_NotFound() {
            MemberStatusUpdateDto dto = new MemberStatusUpdateDto(99L, MemberStatus.DORMANT);

            given(memberRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.updateMemberState(dto))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}