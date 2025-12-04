package store.bookscamp.api.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberStatusTest {

    @Test
    @DisplayName("정상 문자열 입력 시 Enum 변환 성공")
    void from_validValue_returnsEnum() {
        assertThat(MemberStatus.from("NORMAL")).isEqualTo(MemberStatus.NORMAL);
        assertThat(MemberStatus.from("dormant")).isEqualTo(MemberStatus.DORMANT);
        assertThat(MemberStatus.from("Withdrawn")).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("존재하지 않는 문자열 입력 시 null 반환")
    void from_invalidValue_returnsNull() {
        assertThat(MemberStatus.from("INVALID")).isNull();
        assertThat(MemberStatus.from("")).isNull();
        assertThat(MemberStatus.from(" ")).isNull();
    }

    @Test
    @DisplayName("null 입력 시 null 반환")
    void from_nullValue_returnsNull() {
        assertThat(MemberStatus.from(null)).isNull();
    }
}
