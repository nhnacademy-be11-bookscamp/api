package store.bookscamp.api.member.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public String create(MemberCreateDto member) {

        Member newMember = new Member(
                member.name(),
                member.password(),
                member.email(),
                member.phone(),
                0,
                MemberStatus.NORMAL,
                LocalDate.now(),
                member.accountId(),
                null,
                member.birthDate()
        );

        Member savedMember = memberRepository.save(newMember);
        return savedMember.getName();
    }
}
