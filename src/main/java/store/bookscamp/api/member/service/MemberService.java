package store.bookscamp.api.member.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.publisher.MemberEventPublisher;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.member.service.dto.MemberCreateDto;
import store.bookscamp.api.member.service.dto.MemberGetDto;
import store.bookscamp.api.member.service.dto.MemberPasswordUpdateDto;
import store.bookscamp.api.member.service.dto.MemberUpdateDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberEventPublisher memberEventPublisher;

    @Transactional(readOnly = true)
    public MemberGetDto getMember(String id){

        return MemberGetDto.fromEntity(memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                ErrorCode.MEMBER_NOT_FOUND)
        )
        );
    }

    @Transactional(readOnly = true)
    public boolean checkIdDuplicate(String id) {
        return memberRepository.existsByUsername(id);
    }

    @Transactional(readOnly = true)
    public void checkEmailPhoneDuplicate(String email, String phone){
        if(memberRepository.existsByEmail(email)){
            throw new ApplicationException(ErrorCode.EMAIL_DUPLICATE);
        }
        if(memberRepository.existsByPhone(phone)) {
            throw new ApplicationException(ErrorCode.PHONE_DUPLICATE);
        }
    }

    @Transactional
    public void createMember(MemberCreateDto member) {
        Member newMember = new Member(
                member.name(),
                member.password(),
                member.email(),
                member.phone(),
                0,
                MemberStatus.NORMAL,
                LocalDate.now(),
                member.username(),
                null,
                member.birthDate()
        );

        Long memberId = memberRepository.save(newMember).getId();
        memberEventPublisher.publishSignupEvent(memberId);
    }

    @Transactional
    public void updateMember(String id, MemberUpdateDto memberUpdateDto){
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
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
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                        ErrorCode.MEMBER_NOT_FOUND)
        );
        member.changePassword(memberPasswordUpdateDto.password());
    }

    @Transactional
    public void deleteMember(String id){
        Member member = memberRepository.getByUsername(id).orElseThrow(
                () -> new ApplicationException(
                        ErrorCode.MEMBER_NOT_FOUND)
        );
        memberRepository.delete(member);
    }
}
