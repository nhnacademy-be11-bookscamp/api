package store.bookscamp.api.address.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.bookscamp.api.member.entity.Member;

class AddressTest {

    @Test
    @DisplayName("updateAddress - 모든 값이 정상적으로 변경된다")
    void updateAddress_success() {
        // given
        Member member = mock(Member.class);
        Address address = new Address(
                member,
                "집",
                "서울시 강남구 테헤란로",
                12345,
                false,
                "101호"
        );

        // when
        address.updateAddress(
                "회사",
                "서울시 서초구 서초대로",
                54321,
                true,
                "202호"
        );

        // then
        assertThat(address.getLabel()).isEqualTo("회사");
        assertThat(address.getRoadNameAddress()).isEqualTo("서울시 서초구 서초대로");
        assertThat(address.getZipCode()).isEqualTo(54321);
        assertThat(address.isDefault()).isTrue();
        assertThat(address.getDetailAddress()).isEqualTo("202호");
    }
}
