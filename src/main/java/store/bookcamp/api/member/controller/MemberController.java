package store.bookcamp.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.request.MemberUpdateRequest;
import store.bookcamp.api.member.controller.response.MemberGetResponse;
import store.bookcamp.api.member.service.MemberCreateDto;
import store.bookcamp.api.member.service.MemberService;
import store.bookcamp.api.member.service.MemberUpdateDto;

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

    @PostMapping
    @Tag(name = "Member API")
    @Operation(summary = "create Member", description = "회원가입 API")
    public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest){
        MemberCreateDto memberCreateDto = MemberCreateRequest.toDto(memberCreateRequest);
        memberService.create(memberCreateDto);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/login");
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @PutMapping("/{id}")
    @Tag(name = "Member API")
    @Operation(summary = "update Member", description = "회원정보 수정 API")
    public ResponseEntity<Void> updateMember(@PathVariable String id, @Valid @RequestBody MemberUpdateRequest memberUpdateRequest) {
        MemberUpdateDto memberUpdateDto = MemberUpdateRequest.toDto(memberUpdateRequest);
        memberService.update(memberUpdateDto);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/mypage");
        return new ResponseEntity<>(headers,HttpStatus.SEE_OTHER);
    }
}
