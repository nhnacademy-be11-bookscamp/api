package store.bookscamp.api.member.service;

import java.time.LocalDate;
import java.util.Objects;
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
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.rank.entity.Rank;
import store.bookscamp.api.rank.repository.RankRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberEventPublisher memberEventPublisher;
    private final RankRepository rankRepository;

    @Transactional(readOnly = true)
    public MemberGetDto getMember(Long id){
        Member member = memberRepository.getById(id);
        if (Objects.isNull(member)){
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return MemberGetDto.fromEntity(member);
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
        Rank standardRank = rankRepository.findByPointPolicy_PointPolicyType(PointPolicyType.STANDARD)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RANK_NOT_FOUND));

        Member newMember = new Member(
                member.name(),
                member.password(),
                member.email(),
                member.phone(),
                0,
                standardRank,
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
    public void updateMember(Long id, MemberUpdateDto memberUpdateDto){
        Member member = memberRepository.getById(id);
        if(Objects.isNull(member)){
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }

        member.changeInfo(
                memberUpdateDto.name(),
                memberUpdateDto.email(),
                memberUpdateDto.phone()
        );
    }

    public void checkEmailPhoneDuplicateForUpdate(Long currentUserId, String email, String phone) {
        if (memberRepository.existsByEmailAndIdNot(email, currentUserId)) {
            throw new ApplicationException(ErrorCode.EMAIL_DUPLICATE);
        }

        if (memberRepository.existsByPhoneAndIdNot(phone, currentUserId)) {
            throw new ApplicationException(ErrorCode.PHONE_DUPLICATE);
        }
    }

    @Transactional
    public void updateMemberPassoword(Long id, MemberPasswordUpdateDto memberPasswordUpdateDto){
        Member member = memberRepository.getById(id);
        if(Objects.isNull(member)){
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }
        member.changePassword(memberPasswordUpdateDto.password());
    }

    @Transactional
    public void deleteMember(Long id){
        Member member = memberRepository.getById(id);
        if(Objects.isNull(member)){
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.delete(member);
    }
}
