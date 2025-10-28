package store.bookscamp.api.member.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.common.exception.MemberNotFoundException;
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

        return MemberGetDto.fromEntity(memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                ErrorCode.MEMBER_NOT_FOUND)
        )
        );
    }

    public boolean checkIdDuplicate(String id) {
        return memberRepository.existsByUsername(id);
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
                member.username(),
                null,
                member.birthDate()
        );

        memberRepository.save(newMember);
    }

    @Transactional
    public void updateMember(String id, MemberUpdateDto memberUpdateDto){
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new MemberNotFoundException(
                        ErrorCode.MEMBER_NOT_FOUND)
        );

        member.changeInfo(
                memberUpdateDto.name(),
                memberUpdateDto.email(),
                memberUpdateDto.phone()
        );
    }

    @Transactional
    public void updateMemberPassoword(String id, MemberPasswordUpdateDto memberPasswordUpdateDto){
        String encodedPassword = passwordEncoder.encode(memberPasswordUpdateDto.password());
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                        ErrorCode.MEMBER_NOT_FOUND)
        );

        member.changePassword(encodedPassword);
    }

    @Transactional
    public void deleteMember(String id){
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                        ErrorCode.MEMBER_NOT_FOUND)
        );

        member.changeStatus(MemberStatus.WITHDRAWN);
    }
}
