package store.bookscamp.api.address.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.address.entity.Address;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressRepositoryTest {

    @Mock
    private AddressRepository addressRepository;

    @Test
    @DisplayName("findAllByMemberId 호출 시 전달한 memberId로 조회된다 (Mockito 기반)")
    void findAllByMemberId_success() {
        Long memberId = 1L;
        when(addressRepository.findAllByMemberId(memberId))
                .thenReturn(Collections.emptyList());

        var result = addressRepository.findAllByMemberId(memberId);

        assertThat(result).isEmpty();
        verify(addressRepository).findAllByMemberId(memberId);
    }

    @Test
    @DisplayName("countByMemberId 호출 시 반환값 검증 (Mockito 기반)")
    void countByMemberId_success() {
        Long memberId = 1L;
        when(addressRepository.countByMemberId(memberId))
                .thenReturn(3L);

        long count = addressRepository.countByMemberId(memberId);

        assertThat(count).isEqualTo(3L);
        verify(addressRepository).countByMemberId(memberId);
    }

    @Test
    @DisplayName("findByIdAndMemberId 호출 시 Optional<Address> 반환 (Mockito 기반)")
    void findByIdAndMemberId_success() {
        Long memberId = 1L;
        Long addressId = 10L;
        Address address = mock(Address.class);

        when(addressRepository.findByIdAndMemberId(addressId, memberId))
                .thenReturn(Optional.of(address));

        Optional<Address> result = addressRepository.findByIdAndMemberId(addressId, memberId);

        assertThat(result).isPresent();
        verify(addressRepository).findByIdAndMemberId(addressId, memberId);
    }

    @Test
    @DisplayName("clearDefaultForMember 호출 시 수정된 행 수 반환 (Mockito 기반)")
    void clearDefaultForMember_success() {
        Long memberId = 1L;
        Long excludeId = 10L;

        when(addressRepository.clearDefaultForMember(memberId, excludeId))
                .thenReturn(1);

        int updated = addressRepository.clearDefaultForMember(memberId, excludeId);

        assertThat(updated).isEqualTo(1);
        verify(addressRepository).clearDefaultForMember(memberId, excludeId);
    }
}
