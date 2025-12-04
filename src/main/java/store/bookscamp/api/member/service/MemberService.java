package store.bookscamp.api.member.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import store.bookscamp.api.member.service.dto.MemberPageDto;
import store.bookscamp.api.member.service.dto.MemberStatusUpdateDto;
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
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
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
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));

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
    public void deleteMember(Long id){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public Page<MemberPageDto> getAll(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);

        return members.map(member -> new MemberPageDto(
                member.getId(),
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getStatus(),
                member.getLastLoginAt(),
                member.getStatusUpdateDate()
        ));
    }

    @Transactional
    public void updateMemberState(MemberStatusUpdateDto dto){
        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateStatus(dto.status());
        memberRepository.save(member);
    }
}
