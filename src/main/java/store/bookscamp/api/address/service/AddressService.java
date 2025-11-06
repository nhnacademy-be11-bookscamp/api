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

    @Transactional
    public void createMemberAddress(AddressCreateDto addressCreateDto, String username) {

        Member member = memberRepository.getByUsername(username).orElseThrow(
                () -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND)
        );

        Address address = new Address(
                member,
                addressCreateDto.label(),
                addressCreateDto.roadNameAddress(),
                addressCreateDto.zipCode(),
                addressCreateDto.isDefault(),
                addressCreateDto.detailAddress()
        );

        long count = addressRepository.countByMember(member);
        log.info("{}", count);
        if (count >= 10) {
            throw new ApplicationException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        Address saved = addressRepository.save(address);

        if (Boolean.TRUE.equals(addressCreateDto.isDefault())) {
            addressRepository.clearDefaultForMember(member, saved.getId());
        }
    }

    public List<AddressReadDto> getMemberAddresses(String username) {
        List<Address> addresses = addressRepository.getAllByMemberUserName(username);
        return addresses.stream()
                .map(AddressReadDto::from)
                .toList();
    }

    @Transactional
    public void updateMemberAddress(String username,
                                    Long addressId,
                                    AddressUpdateRequestDto addressUpdateRequestDto) {

        Member member = memberRepository.getByUsername(username).orElseThrow(
                () -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND)
        );
        Address address = addressRepository.getByIdAndMemberUserName(addressId, username).orElseThrow(
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
            addressRepository.clearDefaultForMember(address.getMember(), address.getId());
        }

    }

    @Transactional
    public void deleteMemberAddress(String username, Long addressId) {
        Address address = addressRepository.getByIdAndMemberUserName(addressId, username).orElseThrow(
                () -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND)
        );
        addressRepository.delete(address);
    }
}
