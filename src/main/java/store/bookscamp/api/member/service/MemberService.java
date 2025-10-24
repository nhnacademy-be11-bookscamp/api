package store.bookscamp.api.member.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.member.service.dto.MemberCreateDto;
import store.bookscamp.api.member.service.dto.MemberGetDto;
import store.bookscamp.api.member.service.dto.MemberPasswordUpdateDto;
import store.bookscamp.api.member.service.dto.MemberUpdateDto;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberGetDto getMember(String id){
        return MemberGetDto.fromEntity(memberRepository.getByAccountId(id));
    }

    public boolean checkIdDuplicate(String id) {
        return memberRepository.existsByAccountId(id);
    }

    public void createMember(MemberCreateDto member) {
        String encodedPassword = passwordEncoder.encode(member.password());
        Member newMember = new Member(
                member.name(),
                encodedPassword,
                member.email(),
                member.phone(),
                0,
                MemberStatus.NORMAL,
                LocalDate.now(),
                member.accountId(),
                null,
                member.birthDate()
        );

        memberRepository.save(newMember);
    }

    @Transactional
    public void updateMember(String id, MemberUpdateDto memberUpdateDto){
        Member member = memberRepository.getByAccountId(id);

        member.changeInfo(
                memberUpdateDto.name(),
                memberUpdateDto.email(),
                memberUpdateDto.phone()
        );
    }

    @Transactional
    public void updateMemberPassoword(String id, MemberPasswordUpdateDto memberPasswordUpdateDto){
        String encodedPassword = passwordEncoder.encode(memberPasswordUpdateDto.password());
        Member member = memberRepository.getByAccountId(id);

        member.changePassword(encodedPassword);
    }

    @Transactional
    public void deleteMember(String id){
        Member member = memberRepository.getByAccountId(id);

        member.changeStatus(MemberStatus.WITHDRAWN);
    }
}
