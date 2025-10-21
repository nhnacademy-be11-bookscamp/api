package store.bookcamp.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.response.MemberCreateResponse;
import store.bookcamp.api.member.service.MemberDto;
import store.bookcamp.api.member.service.MemberService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
@Tag(name = "Member API", description = "Member CRU API입니다")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Tag(name = "Member API")
    @Operation(summary = "create", description = "회원가입 api")
    public ResponseEntity<MemberCreateResponse> createMember(@RequestBody MemberCreateRequest memberCreateRequest){
        MemberDto memberDto = new MemberDto(
                memberCreateRequest.id(),
                memberCreateRequest.password(),
                memberCreateRequest.name(),
                memberCreateRequest.email(),
                memberCreateRequest.phone(),
                memberCreateRequest.birthDate()
        );
        String response = memberService.create(memberDto);
        MemberCreateResponse memberCreateResponse = new MemberCreateResponse(response);
        return new ResponseEntity<>(memberCreateResponse, HttpStatus.CREATED);
    }
}
