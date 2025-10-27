package store.bookscamp.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("/{id}")
    @Tag(name = "Member API")
    @Operation(summary = "read Member", description = "회원조희 API")
    public MemberGetResponse getMember(@PathVariable String id){
        return MemberGetResponse.fromDto(memberService.getMember(id));
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
        memberService.createMember(memberCreateDto);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/login");
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @PutMapping("/{id}")
    @Tag(name = "Member API")
    @Operation(summary = "update Member", description = "회원정보 수정 API")
    public ResponseEntity<MemberGetResponse> updateMember(@PathVariable String id, @Valid @RequestBody MemberUpdateRequest memberUpdateRequest) {
        MemberUpdateDto memberUpdateDto = MemberUpdateRequest.toDto(memberUpdateRequest);
        memberService.updateMember(id, memberUpdateDto);
        MemberGetResponse memberGetResponse = MemberGetResponse.fromDto(memberService.getMember(id));
        return new ResponseEntity<>(memberGetResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}/change-password")
    @Tag(name = "Member API")
    @Operation(summary = "update password", description = "비밀번호 수정 API")
        public ResponseEntity<Void> updatePassword(@PathVariable String id, @Valid @RequestBody MemberPasswordUpdateRequest memberPasswordUpdateRequest) {
        MemberPasswordUpdateDto memberPasswordUpdateDto = MemberPasswordUpdateRequest.toDto(memberPasswordUpdateRequest);
        memberService.updateMemberPassoword(id,memberPasswordUpdateDto);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @DeleteMapping("/{id}")
    @Tag(name = "Member API")
    @Operation(summary = "delete Member", description = "회원탈퇴 API")
    public ResponseEntity<Void> deleteMember(@PathVariable String id){
        memberService.deleteMember(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/login");
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }
}
