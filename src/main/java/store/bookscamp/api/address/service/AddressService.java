package store.bookscamp.api.address.service;

import java.util.List;
import org.springframework.stereotype.Service;
import store.bookscamp.api.address.entity.Address;
import store.bookscamp.api.address.repository.AddressRepository;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Service
public class AddressService {
    private AddressRepository addressRepository;
    private MemberRepository memberRepository;

    public void createMemberAddress(AddressCreateDto addressCreateDto, String username) {
        Member member = memberRepository.getByAccountId(username);
        Address address = new Address(
                member,
                addressCreateDto.Label(),
                addressCreateDto.roadNameAddress(),
                addressCreateDto.zipCode()

        );
        addressRepository.save(address);
    }
}
