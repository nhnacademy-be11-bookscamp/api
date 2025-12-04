package store.bookscamp.api.packaging.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PackagingTest {

    @Test
    @DisplayName("change - 모든 값이 정상적으로 변경된다")
    void change_success() {
        // given
        Packaging packaging = new Packaging("기본 포장", 1000, "url1");

        // when
        packaging.change("프리미엄 포장", 2000, "url2");

        // then
        assertThat(packaging.getName()).isEqualTo("프리미엄 포장");
        assertThat(packaging.getPrice()).isEqualTo(2000);
        assertThat(packaging.getImageUrl()).isEqualTo("url2");
    }

    @Test
    @DisplayName("change - null 값은 기존 값을 유지한다")
    void change_keepOriginalWhenNull() {
        // given
        Packaging packaging = new Packaging("기본 포장", 1000, "url1");

        // when
        packaging.change(null, null, null);

        // then
        assertThat(packaging.getName()).isEqualTo("기본 포장");
        assertThat(packaging.getPrice()).isEqualTo(1000);
        assertThat(packaging.getImageUrl()).isEqualTo("url1");
    }

    @Test
    @DisplayName("change - 일부 변경만 반영되고 나머지는 유지된다")
    void change_partialUpdate() {
        // given
        Packaging packaging = new Packaging("기본 포장", 1000, "url1");

        // when
        packaging.change("새 포장", null, "url2");

        // then
        assertThat(packaging.getName()).isEqualTo("새 포장");
        assertThat(packaging.getPrice()).isEqualTo(1000);
        assertThat(packaging.getImageUrl()).isEqualTo("url2");
    }
}
