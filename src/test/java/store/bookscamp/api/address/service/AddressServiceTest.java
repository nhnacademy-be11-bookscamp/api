package store.bookscamp.api.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.address.entity.Address;
import store.bookscamp.api.address.repository.AddressRepository;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member createMember(Long id) {
        Member member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(id);
        return member;
    }

    private Address createAddress(Long id, Long memberId) {
        Address address = mock(Address.class);
        lenient().when(address.getId()).thenReturn(id);
        lenient().when(address.getLabel()).thenReturn("집");
        lenient().when(address.getRoadNameAddress()).thenReturn("서울시 어딘가 1로");
        lenient().when(address.getZipCode()).thenReturn(12345);
        lenient().when(address.isDefault()).thenReturn(true);
        lenient().when(address.getDetailAddress()).thenReturn("101호");
        return address;
    }

    @Nested
    @DisplayName("회원 주소 생성 (createMemberAddress)")
    class CreateAddressTest {

        @Test
        @DisplayName("성공: 회원 존재, 주소 개수 10개 미만, isDefault = true")
        void createMemberAddress_success_defaultTrue() {
            Long memberId = 1L;
            Member member = createMember(memberId);

            AddressCreateDto dto = new AddressCreateDto(
                    "집",
                    "서울시 어딘가 1로",
                    12345,
                    true,
                    "101호"
            );

            Address saved = mock(Address.class);
            given(saved.getId()).willReturn(100L);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(addressRepository.countByMemberId(memberId)).willReturn(0L);
            given(addressRepository.save(any(Address.class))).willReturn(saved);

            addressService.createMemberAddress(memberId, dto);

            verify(addressRepository).save(any(Address.class));
            verify(addressRepository).clearDefaultForMember(memberId, 100L);
        }

        @Test
        @DisplayName("실패: 회원을 찾을 수 없음 (MEMBER_NOT_FOUND)")
        void createMemberAddress_fail_memberNotFound() {
            Long memberId = 1L;
            AddressCreateDto dto = new AddressCreateDto(
                    "집", "서울", 12345, true, "101호"
            );

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.createMemberAddress(memberId, dto))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 주소 개수 10개 이상 (ADDRESS_LIMIT_EXCEEDED)")
        void createMemberAddress_fail_addressLimitExceeded() {
            Long memberId = 1L;
            Member member = createMember(memberId);
            AddressCreateDto dto = new AddressCreateDto(
                    "집", "서울", 12345, true, "101호"
            );

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(addressRepository.countByMemberId(memberId)).willReturn(10L);

            assertThatThrownBy(() -> addressService.createMemberAddress(memberId, dto))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("회원 주소 목록 조회 (getMemberAddresses)")
    class GetAddressesTest {

        @Test
        @DisplayName("성공: 조회된 Address 수만큼 DTO 리스트 반환")
        void getMemberAddresses_success() {
            Long memberId = 1L;
            Address address = createAddress(100L, memberId);
            given(addressRepository.findAllByMemberId(memberId))
                    .willReturn(List.of(address));

            List<AddressReadDto> result = addressService.getMemberAddresses(memberId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(100L);
            verify(addressRepository).findAllByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("회원 주소 수정 (updateMemberAddress)")
    class UpdateAddressTest {

        @Test
        @DisplayName("성공: 주소 존재, isDefault = true 인 경우 clearDefaultForMember 호출")
        void updateMemberAddress_success_defaultTrue() {
            Long memberId = 1L;
            Long addressId = 100L;

            AddressUpdateRequestDto dto = new AddressUpdateRequestDto(
                    "회사",
                    "서울시 어딘가 2로",
                    54321,
                    true,
                    "202호"
            );

            Address address = createAddress(addressId, memberId);
            given(addressRepository.findByIdAndMemberId(addressId, memberId))
                    .willReturn(Optional.of(address));

            addressService.updateMemberAddress(memberId, addressId, dto);

            verify(address).updateAddress(
                    eq("회사"),
                    eq("서울시 어딘가 2로"),
                    eq(54321),
                    eq(true),
                    eq("202호")
            );
            verify(addressRepository).clearDefaultForMember(memberId, addressId);
        }

        @Test
        @DisplayName("실패: 해당 회원의 주소가 아닌 경우 (ADDRESS_NOT_FOUND)")
        void updateMemberAddress_fail_notFound() {
            Long memberId = 1L;
            Long addressId = 100L;
            AddressUpdateRequestDto dto = new AddressUpdateRequestDto(
                    "회사", "서울", 12345, true, "상세"
            );

            given(addressRepository.findByIdAndMemberId(addressId, memberId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateMemberAddress(memberId, addressId, dto))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원 주소 삭제 (deleteMemberAddress)")
    class DeleteAddressTest {

        @Test
        @DisplayName("성공: 주소 존재 시 삭제 호출")
        void deleteMemberAddress_success() {
            Long memberId = 1L;
            Long addressId = 100L;

            Address address = createAddress(addressId, memberId);
            given(addressRepository.findByIdAndMemberId(addressId, memberId))
                    .willReturn(Optional.of(address));

            addressService.deleteMemberAddress(memberId, addressId);

            verify(addressRepository).delete(address);
        }

        @Test
        @DisplayName("실패: 주소가 존재하지 않는 경우 (ADDRESS_NOT_FOUND)")
        void deleteMemberAddress_fail_notFound() {
            Long memberId = 1L;
            Long addressId = 100L;

            given(addressRepository.findByIdAndMemberId(addressId, memberId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.deleteMemberAddress(memberId, addressId))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_NOT_FOUND);
        }
    }
}
