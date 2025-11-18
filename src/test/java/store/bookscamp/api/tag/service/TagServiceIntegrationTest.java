package store.bookscamp.api.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient; // FIX: lenient static import 추가
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // [FIX 1]: MockitoSettings import 추가
import org.mockito.quality.Strictness; // [FIX 1]: Strictness import 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.exception.TagAlreadyExists;
import store.bookscamp.api.tag.exception.TagNotFoundException;
import store.bookscamp.api.tag.repository.TagRepository;
import store.bookscamp.api.tag.service.dto.TagCreateDto;
import store.bookscamp.api.tag.service.dto.TagGetDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TagServiceTest {

    @InjectMocks
    TagService tagService;

    @Mock
    TagRepository tagRepository;

    private Tag createTestTag(Long id, String name) {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(id);
        when(tag.getName()).thenReturn(name);
        return tag;
    }

    private TagCreateDto createCreateDto(String name) {
        return new TagCreateDto(name);
    }

    private TagGetDto createGetDto(Long id, String name) {
        return new TagGetDto(id, name);
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("태그 생성 성공")
    void create_success() {
        TagCreateDto createDto = createCreateDto("Java");
        String tagName = createDto.getName();

        when(tagRepository.existsByName(tagName)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            return createTestTag(1L, tagName);
        });

        TagGetDto result = tagService.create(createDto);

        assertThat(result.getName()).isEqualTo(tagName);
        verify(tagRepository, times(1)).existsByName(tagName);
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @DisplayName("태그 생성 실패: 이미 존재하는 이름")
    void create_fail_already_exists() {
        TagCreateDto createDto = createCreateDto("Duplicate");
        when(tagRepository.existsByName("Duplicate")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(createDto))
                .isInstanceOf(TagAlreadyExists.class);

        verify(tagRepository, times(1)).existsByName("Duplicate");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("ID로 태그 조회 성공")
    void getById_success() {
        Long tagId = 1L;
        Tag mockTag = createTestTag(tagId, "Spring");
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));
        TagGetDto expectedDto = createGetDto(tagId, "Spring");

        TagGetDto result = tagService.getById(tagId);

        assertThat(result.getId()).isEqualTo(tagId);
        assertThat(result.getName()).isEqualTo("Spring");
        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    @DisplayName("ID로 태그 조회 실패: 자원 없음")
    void getById_fail_not_found() {
        Long tagId = 999L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getById(tagId))
                .isInstanceOf(TagNotFoundException.class);
        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    @DisplayName("전체 태그 목록 조회 성공")
    void getAll_success() {
        Tag tag1 = createTestTag(1L, "T1");
        Tag tag2 = createTestTag(2L, "T2");
        List<Tag> mockTags = List.of(tag1, tag2);
        when(tagRepository.findAll()).thenReturn(mockTags);

        List<TagGetDto> result = tagService.getAll();

        assertThat(result).hasSize(2);
        verify(tagRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("페이징된 태그 목록 조회 성공")
    void getPage_success() {
        Pageable pageable = mock(Pageable.class);
        Tag tag1 = createTestTag(1L, "PageTag1");
        Page<Tag> mockPage = new PageImpl<>(List.of(tag1), pageable, 1);
        when(tagRepository.findAll(pageable)).thenReturn(mockPage);

        Page<TagGetDto> resultPage = tagService.getPage(pageable);

        assertThat(resultPage).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        verify(tagRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("태그 수정 성공: 이름 변경")
    void update_success_name_change() {
        Long tagId = 1L;
        String oldName = "OldName";
        String newName = "NewName";

        Tag mockTag = createTestTag(tagId, oldName);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));

        when(tagRepository.existsByName(newName)).thenReturn(false);

        TagGetDto result = tagService.update(tagId, createCreateDto(newName));

        verify(mockTag, times(1)).changeName(newName);
        verify(tagRepository, times(1)).existsByName(newName);
        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    @DisplayName("태그 수정 성공: 이름 변경 없음")
    void update_success_name_no_change() {
        Long tagId = 1L;
        String name = "SameName";

        Tag mockTag = createTestTag(tagId, name);
        when(mockTag.getName()).thenReturn(name); // 현재 이름 반환하도록 설정
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));

        tagService.update(tagId, createCreateDto(name));

        verify(tagRepository, never()).existsByName(any());
        verify(mockTag, times(1)).changeName(name);
    }

    @Test
    @DisplayName("태그 수정 실패: ID를 찾을 수 없음")
    void update_fail_not_found() {
        Long tagId = 999L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.update(tagId, createCreateDto("New")))
                .isInstanceOf(TagNotFoundException.class);

        verify(tagRepository, never()).existsByName(any());
    }

    @Test
    @DisplayName("태그 수정 실패: 변경하려는 이름이 이미 존재함 (다른 태그와 중복)")
    void update_fail_duplicate_name() {
        Long tagId = 1L;
        String oldName = "A";
        String duplicateName = "B";

        Tag mockTag = createTestTag(tagId, oldName);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));

        when(tagRepository.existsByName(duplicateName)).thenReturn(true);

        assertThatThrownBy(() -> tagService.update(tagId, createCreateDto(duplicateName)))
                .isInstanceOf(TagAlreadyExists.class);

        verify(mockTag, never()).changeName(any());
        verify(tagRepository, times(1)).existsByName(duplicateName);
    }

    @Test
    @DisplayName("태그 삭제 성공")
    void deleteById_success() {
        Long tagId = 1L;
        when(tagRepository.existsById(tagId)).thenReturn(true);

        tagService.deleteById(tagId);

        verify(tagRepository, times(1)).existsById(tagId);
        verify(tagRepository, times(1)).deleteById(tagId);
    }

    @Test
    @DisplayName("태그 삭제 실패: ID를 찾을 수 없음")
    void deleteById_fail_not_found() {
        Long tagId = 999L;
        when(tagRepository.existsById(tagId)).thenReturn(false);

        assertThatThrownBy(() -> tagService.deleteById(tagId))
                .isInstanceOf(TagNotFoundException.class);

        verify(tagRepository, times(1)).existsById(tagId);
        verify(tagRepository, never()).deleteById(anyLong()); // 삭제 호출 안 됨 확인
    }
}