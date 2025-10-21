package store.bookcamp.api.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.response.MemberCreateResponse;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberCreateResponse create(MemberCreateRequest request) {
        Member newMember = new Member(
                request.id(),
                request.password(),
                request.name(),
                request.email(),
                request.phone(),
                request.birth()
        );
        Member savedMember = memberRepository.save(newMember);
        return new MemberCreateResponse(savedMember.getName());
    }
}
