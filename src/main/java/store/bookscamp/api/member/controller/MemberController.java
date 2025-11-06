package store.bookscamp.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.member.controller.request.MemberCreateRequest;
import store.bookscamp.api.member.controller.request.MemberPasswordUpdateRequest;
import store.bookscamp.api.member.controller.request.MemberUpdateRequest;
import store.bookscamp.api.member.controller.response.MemberGetResponse;
import store.bookscamp.api.member.service.dto.MemberCreateDto;
import store.bookscamp.api.member.service.dto.MemberPasswordUpdateDto;
import store.bookscamp.api.member.service.MemberService;
import store.bookscamp.api.member.service.dto.MemberUpdateDto;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
@Tag(name = "Member API", description = "Member CRUD API입니다")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @Tag(name = "Member API")
    @Operation(summary = "read Member", description = "회원조희 API")
    public MemberGetResponse getMember(HttpServletRequest request){
        return MemberGetResponse.fromDto(memberService.getMember(Long.parseLong(request.getHeader("X-User-ID"))));
    }

    @GetMapping("/check-id")
    @Tag(name = "Member API")
    @Operation(summary = "check id", description = "회원중복검사")
    public ResponseEntity<String> checkIdDuplicate(@RequestParam("id")String id){
        boolean isDuplicate = memberService.checkIdDuplicate(id);
        if (isDuplicate) {
            return new ResponseEntity<>("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("사용 가능한 아이디입니다.", HttpStatus.OK);
        }
    }

    @PostMapping
    @Tag(name = "Member API")
    @Operation(summary = "create Member", description = "회원가입 API")
    public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest){
        MemberCreateDto memberCreateDto = MemberCreateRequest.toDto(memberCreateRequest);
        memberService.checkEmailPhoneDuplicate(memberCreateDto.email(),memberCreateDto.phone());
        memberService.createMember(memberCreateDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping
    @Tag(name = "Member API")
    @Operation(summary = "update Member", description = "회원정보 수정 API")
    public ResponseEntity<MemberGetResponse> updateMember(@Valid @RequestBody MemberUpdateRequest memberUpdateRequest,HttpServletRequest request) {

        MemberUpdateDto memberUpdateDto = MemberUpdateRequest.toDto(memberUpdateRequest);
        memberService.checkEmailPhoneDuplicate(memberUpdateDto.email(),memberUpdateDto.phone());
        memberService.updateMember(Long.parseLong(request.getHeader("X-User-ID")), memberUpdateDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/change-password")
    @Tag(name = "Member API")
    @Operation(summary = "update password", description = "비밀번호 수정 API")
        public ResponseEntity<Void> updatePassword(@Valid @RequestBody MemberPasswordUpdateRequest memberPasswordUpdateRequest, HttpServletRequest request) {
        MemberPasswordUpdateDto memberPasswordUpdateDto = MemberPasswordUpdateRequest.toDto(memberPasswordUpdateRequest);
        memberService.updateMemberPassoword(Long.parseLong(request.getHeader("X-User-ID")),memberPasswordUpdateDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @Tag(name = "Member API")
    @Operation(summary = "delete Member", description = "회원탈퇴 API")
    public ResponseEntity<Void> deleteMember(HttpServletRequest request){
        memberService.deleteMember(Long.parseLong(request.getHeader("X-User-ID")));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
