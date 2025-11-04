package store.bookscamp.api.address.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

        if (addressRepository.countByMemberUsername(username) >= 10) {
            throw new ApplicationException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        } else {
            addressRepository.save(address);
        }

    }

    public List<AddressReadDto> getMemberAddresses(String username) {
        List<Address> addresses = addressRepository.getAllByMemberUserId(username);
        return addresses.stream()
                .map(AddressReadDto::from)
                .toList();
    }

    @Transactional
    public void updateMemberAddress(String username, Integer addressId,
                                    AddressUpdateRequestDto addressUpdateRequestDto) {

        Member member = memberRepository.getByUsername(username).orElseThrow(
                () -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND)
        );
        Address address = addressRepository.getByIdAndMemberUserId(addressId, username).orElseThrow(
                () -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND)
        );

        address.updateAddress(
                addressUpdateRequestDto.label(),
                addressUpdateRequestDto.roadNameAddress(),
                addressUpdateRequestDto.zipCode(),
                addressUpdateRequestDto.isDefault(),
                addressUpdateRequestDto.detailAddress()
        );

    }

    @Transactional
    public void deleteMemberAddress(String username, Integer addressId) {
        Address address = addressRepository.getByIdAndMemberUserId(addressId, username).orElseThrow(
                () -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND)
        );
        addressRepository.delete(address);
    }
}
