package store.bookcamp.api.member.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookcamp.api.member.entity.Member;
import store.bookcamp.api.member.entity.Status;
import store.bookcamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberGetDto getMember(String id){
        return MemberGetDto.fromEntity(memberRepository.getByAccountId(id));
    }

    public void createMember(MemberCreateDto member) {

        Member newMember = new Member(
                null,
                member.accountId(),
                member.password(),
                member.name(),
                member.email(),
                member.phone(),
                0,
                Status.NORMAL,
                LocalDate.now(),
                null,
                member.birthDate()
        );

        Member savedMember = memberRepository.save(newMember);
    }

    public MemberGetDto updateMember
}
