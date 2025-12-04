package store.bookscamp.api.category.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    @DisplayName("updateName - 정상적인 이름 입력 시 변경된다")
    void updateName_success() {
        // given
        Category category = new Category(null, "기존");

        // when
        category.updateName("신규");

        // then
        assertThat(category.getName()).isEqualTo("신규");
    }

    @Test
    @DisplayName("updateName - null 입력 시 기존 이름 유지")
    void updateName_null_keepOriginal() {
        Category category = new Category(null, "기존");

        category.updateName(null);

        assertThat(category.getName()).isEqualTo("기존");
    }

    @Test
    @DisplayName("updateName - 빈 문자열 입력 시 기존 이름 유지")
    void updateName_empty_keepOriginal() {
        Category category = new Category(null, "기존");

        category.updateName("");

        assertThat(category.getName()).isEqualTo("기존");
    }

    @Test
    @DisplayName("updateName - 공백 문자열 입력 시 기존 이름 유지")
    void updateName_blank_keepOriginal() {
        Category category = new Category(null, "기존");

        category.updateName("   ");

        assertThat(category.getName()).isEqualTo("기존");
    }
}
