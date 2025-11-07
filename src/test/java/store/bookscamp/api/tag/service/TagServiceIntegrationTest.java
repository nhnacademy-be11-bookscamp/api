package store.bookscamp.api.tag.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.exception.TagAlreadyExists;
import store.bookscamp.api.tag.exception.TagNotFoundException;
import store.bookscamp.api.tag.repository.TagRepository;
import store.bookscamp.api.tag.service.dto.TagCreateDto;
import store.bookscamp.api.tag.service.dto.TagGetDto;

@SpringBootTest
@Transactional
class TagServiceIntegrationTest {

    @Autowired
    TagService tagService;

    @Autowired
    TagRepository tagRepository;

    @Nested
    @DisplayName("create")
    class CreateTests {
        @Test
        @DisplayName("성공 - 새 태그 생성")
        void create_ok() {
            TagGetDto dto = tagService.create(new TagCreateDto("java"));

            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getName()).isEqualTo("java");

            Tag saved = tagRepository.findById(dto.getId()).orElseThrow();
            assertThat(saved.getName()).isEqualTo("java");
        }

        @Test
        @DisplayName("실패 - 이름 중복 시 TagAlreadyExists")
        void create_dup() {
            tagRepository.save(Tag.create("java"));

            assertThatThrownBy(() -> tagService.create(new TagCreateDto("java")))
                    .isInstanceOf(TagAlreadyExists.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {
        @Test
        @DisplayName("성공 - ID로 조회")
        void get_ok() {
            Tag saved = tagRepository.save(Tag.create("spring"));

            TagGetDto dto = tagService.getById(saved.getId());

            assertThat(dto.getId()).isEqualTo(saved.getId());
            assertThat(dto.getName()).isEqualTo("spring");
        }

        @Test
        @DisplayName("실패 - 없으면 NotFound")
        void get_notfound() {
            assertThatThrownBy(() -> tagService.getById(999L))
                    .isInstanceOf(TagNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("getAll - 전체 반환")
    void getAll_ok() {
        tagRepository.save(Tag.create("a"));
        tagRepository.save(Tag.create("b"));

        List<TagGetDto> all = tagService.getAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TagGetDto::getName).containsExactlyInAnyOrder("a", "b");
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {
        @Test
        @DisplayName("성공 - 이름 변경")
        void update_ok() {
            Tag saved = tagRepository.save(Tag.create("old"));

            TagGetDto updated = tagService.update(saved.getId(), new TagCreateDto("new"));

            assertThat(updated.getName()).isEqualTo("new");
            assertThat(tagRepository.findById(saved.getId()).orElseThrow().getName())
                    .isEqualTo("new");
        }

        @Test
        @DisplayName("실패 - 다른 항목과 이름 중복")
        void update_dup() {
            Tag t1 = tagRepository.save(Tag.create("keep"));
            tagRepository.save(Tag.create("dup"));

            assertThatThrownBy(() -> tagService.update(t1.getId(), new TagCreateDto("dup")))
                    .isInstanceOf(TagAlreadyExists.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("실패 - 없는 ID")
        void update_notfound() {
            assertThatThrownBy(() -> tagService.update(777L, new TagCreateDto("x")))
                    .isInstanceOf(TagNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteTests {
        @Test
        @DisplayName("성공 - 삭제")
        void delete_ok() {
            Tag saved = tagRepository.save(Tag.create("del"));

            tagService.deleteById(saved.getId());

            assertThat(tagRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("실패 - 없는 ID")
        void delete_notfound() {
            assertThatThrownBy(() -> tagService.deleteById(1L))
                    .isInstanceOf(TagNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_FOUND);
        }
    }
}
