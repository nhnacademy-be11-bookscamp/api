package store.bookcamp.api.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public String create(MemberDto member) {
        Member newMember = new Member(member);
        Member savedMember = memberRepository.save(newMember);
        return savedMember.getName();
    }
}
