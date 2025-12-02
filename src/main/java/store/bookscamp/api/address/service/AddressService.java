package store.bookscamp.api.address.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.address.entity.Address;
import store.bookscamp.api.address.repository.AddressRepository;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    /**
     * 회원 주소 생성
     */
    @Transactional
    public void createMemberAddress(Long memberId, AddressCreateDto addressCreateDto) {

        // memberId로 회원 존재 여부 확인 및 연관관계 설정용 엔티티 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND)
        );

        long count = addressRepository.countByMemberId(memberId);
        log.info("memberId={} address count={}", memberId, count);
        if (count >= 10) {
            throw new ApplicationException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        Address address = new Address(
                member,
                addressCreateDto.label(),
                addressCreateDto.roadNameAddress(),
                addressCreateDto.zipCode(),
                addressCreateDto.isDefault(),
                addressCreateDto.detailAddress()
        );

        Address saved = addressRepository.save(address);

        if (Boolean.TRUE.equals(addressCreateDto.isDefault())) {
            // 해당 회원의 다른 기본 주소들 false 처리
            addressRepository.clearDefaultForMember(memberId, saved.getId());
        }
    }

    /**
     * 회원 주소 목록 조회
     */
    public List<AddressReadDto> getMemberAddresses(Long memberId) {
        List<Address> addresses = addressRepository.findAllByMemberId(memberId);
        return addresses.stream()
                .map(AddressReadDto::from)
                .toList();
    }

    /**
     * 회원 주소 수정
     */
    @Transactional
    public void updateMemberAddress(Long memberId,
                                    Long addressId,
                                    AddressUpdateRequestDto addressUpdateRequestDto) {

        // 이 회원의 주소가 맞는지 검증하면서 조회
        Address address = addressRepository.findByIdAndMemberId(addressId, memberId).orElseThrow(
                () -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND)
        );

        address.updateAddress(
                addressUpdateRequestDto.label(),
                addressUpdateRequestDto.roadNameAddress(),
                addressUpdateRequestDto.zipCode(),
                addressUpdateRequestDto.isDefault(),
                addressUpdateRequestDto.detailAddress()
        );

        if (addressUpdateRequestDto.isDefault()) {
            // 이 회원의 다른 기본 주소들 false 처리
            addressRepository.clearDefaultForMember(memberId, address.getId());
        }
    }

    /**
     * 회원 주소 삭제
     */
    @Transactional
    public void deleteMemberAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findByIdAndMemberId(addressId, memberId).orElseThrow(
                () -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND)
        );
        addressRepository.delete(address);
    }
}
