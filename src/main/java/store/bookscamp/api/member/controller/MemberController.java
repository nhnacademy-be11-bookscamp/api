package store.bookscamp.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.common.pagination.RestPageImpl;
import store.bookscamp.api.member.controller.request.MemberCreateRequest;
import store.bookscamp.api.member.controller.request.MemberStatusUpdateRequest;
import store.bookscamp.api.member.controller.request.MemberUpdateRequest;
import store.bookscamp.api.member.controller.response.MemberGetResponse;
import store.bookscamp.api.member.controller.response.MemberPageResponse;
import store.bookscamp.api.member.service.dto.MemberCreateDto;
import store.bookscamp.api.member.service.MemberService;
import store.bookscamp.api.member.service.dto.MemberPageDto;
import store.bookscamp.api.member.service.dto.MemberStatusUpdateDto;
import store.bookscamp.api.member.service.dto.MemberUpdateDto;

@RequiredArgsConstructor
@RestController
@Tag(name = "회원 API", description = "Member CRUD API입니다")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member")
    @Operation(summary = "read Member", description = "회원조희 API")
    @RequiredRole("USER")
    public MemberGetResponse getMember(HttpServletRequest request) {
        return MemberGetResponse.fromDto(memberService.getMember(Long.parseLong(request.getHeader("X-User-ID"))));
    }

    @GetMapping("/member/check-id")
    @Operation(summary = "check id", description = "회원중복검사")
    public ResponseEntity<String> checkIdDuplicate(@RequestParam("id") String id) {
        boolean isDuplicate = memberService.checkIdDuplicate(id);
        if (isDuplicate) {
            return new ResponseEntity<>("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("사용 가능한 아이디입니다.", HttpStatus.OK);
        }
    }

    @PostMapping("/member/sign-up")
    @Operation(summary = "create Member", description = "회원가입 API")
    public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        MemberCreateDto memberCreateDto = MemberCreateRequest.toDto(memberCreateRequest);
        memberService.checkEmailPhoneDuplicate(memberCreateDto.email(), memberCreateDto.phone());
        memberService.createMember(memberCreateDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/member")
    @Operation(summary = "update Member", description = "회원정보 수정 API")
    @RequiredRole("USER")
    public ResponseEntity<MemberGetResponse> updateMember(
            @Valid @RequestBody MemberUpdateRequest memberUpdateRequest,
            HttpServletRequest request) {
        Long currentUserId = Long.parseLong(request.getHeader("X-User-ID"));
        MemberUpdateDto memberUpdateDto = MemberUpdateRequest.toDto(memberUpdateRequest);
        memberService.checkEmailPhoneDuplicateForUpdate(
                currentUserId,
                memberUpdateDto.email(),
                memberUpdateDto.phone()
        );
        memberService.updateMember(currentUserId, memberUpdateDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/member")
    @Operation(summary = "delete Member", description = "회원탈퇴 API")
    @RequiredRole("USER")
    public ResponseEntity<Void> deleteMember(HttpServletRequest request) {
        memberService.deleteMember(Long.parseLong(request.getHeader("X-User-ID")));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/admin/member")
    @Operation(summary = "get All Members", description = "전체 회원 조회 API")
    @RequiredRole("ADMIN")
    public ResponseEntity<RestPageImpl<MemberPageResponse>> getAllMembers(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MemberPageDto> dtoPage = memberService.getAll(pageable);

        Page<MemberPageResponse> responsePage = dtoPage.map(dto -> new MemberPageResponse(
                dto.id(),
                dto.username(),
                dto.name(),
                dto.email(),
                dto.phone(),
                dto.status().name(),
                dto.lastLoginAt(),
                dto.statusUpdateDate()
        ));

        return new ResponseEntity<>(
                new RestPageImpl<>(responsePage), HttpStatus.OK);
    }

    @PutMapping("/admin/member/updateStatus")
    @Operation(summary = "update memberStatus", description = "회원 상태 데이트 API")
    @RequiredRole("ADMIN")
    public ResponseEntity<Void> updateMemberStatus(@Valid @RequestBody MemberStatusUpdateRequest request){
        MemberStatusUpdateDto dto = MemberStatusUpdateRequest.toDto(request);
        memberService.updateMemberState(dto);
        return ResponseEntity.ok().build();
    }
}