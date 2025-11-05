// TagRepositoryTest

package store.bookscamp.api.tag.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import store.bookscamp.api.tag.entity.Tag;

@DataJpaTest
@EntityScan(basePackages = "store.bookscamp.api.tag.entity")
@EnableJpaRepositories(basePackages = "store.bookscamp.api.tag.repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // (선택) H2 강제
class TagRepositoryTest {

    @Autowired
    TagRepository tagRepository;

    Tag tag1;
    Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = tagRepository.save(Tag.create("Spring"));
        tag2 = tagRepository.save(Tag.create("Java"));
    }

    @Test
    @DisplayName("existsByName - 존재함")
    void existsByName_true() {
        assertThat(tagRepository.existsByName("Spring")).isTrue();
    }

    @Test
    @DisplayName("existsByName - 존재하지 않음")
    void existsByName_false() {
        assertThat(tagRepository.existsByName("NonExistentTag")).isFalse();
    }

    @Test
    @DisplayName("findByName - 성공")
    void findByName_found() {
        assertThat(tagRepository.findByName("Java"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("Java");
    }

    @Test
    @DisplayName("findByName - 실패")
    void findByName_empty() {
        assertThat(tagRepository.findByName("NotFound")).isNotPresent();
    }

    @Test
    @DisplayName("save - ID 생성 및 조회 가능")
    void save_generatesId() {
        Tag saved = tagRepository.save(Tag.create("TestTag"));
        assertThat(saved.getId()).isNotNull().isPositive();

        assertThat(tagRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("TestTag");
    }

    @Test
    @DisplayName("findAll - 2건")
    void findAll_returnsAll() {
        assertThat(tagRepository.findAll())
                .hasSize(2)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "Java");
    }

    @Test
    @DisplayName("deleteById - 삭제")
    void deleteById_removes() {
        Long idToDelete = tag1.getId();

        tagRepository.deleteById(idToDelete);

        assertThat(tagRepository.findById(idToDelete)).isNotPresent();
        assertThat(tagRepository.findAll())
                .hasSize(1)
                .extracting(Tag::getName)
                .containsExactly("Java");
    }
}
