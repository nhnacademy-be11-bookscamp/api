package store.bookcamp.api.member.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookcamp.api.member.controller.request.MemberCreateRequest;
import store.bookcamp.api.member.controller.response.MemberCreateResponse;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.State;
import store.bookcamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public MemberCreateResponse create(MemberCreateRequest request) {
        Member newMember = new Member();
        newMember.setAccountId(request.id());
        newMember.setPassword(request.password());
        newMember.setName(request.name());
        newMember.setEmail(request.email());
        newMember.setPhone(request.phone());
        newMember.setBirth(request.birth());
        newMember.setPoint(0);
        newMember.setState(State.NORMAL);
        newMember.setStatusUpdateDate(LocalDate.now());
        newMember.setLastLoginAt(null);
        Member savedMember = memberRepository.save(newMember);
        return new MemberCreateResponse(savedMember.getName());
    }
}
