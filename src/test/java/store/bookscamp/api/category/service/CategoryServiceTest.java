package store.bookscamp.api.category.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryCreateDto;
import store.bookscamp.api.category.service.dto.CategoryDeleteDto;
import store.bookscamp.api.category.service.dto.CategoryListDto;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("getCategoryTree - 카테고리 트리 구조 생성 성공")
    void getCategoryTree_Success() {
        // given
        Category parent = mock(Category.class);
        when(parent.getId()).thenReturn(1L);
        when(parent.getName()).thenReturn("부모");
        when(parent.getParent()).thenReturn(null);

        Category child = mock(Category.class);
        when(child.getId()).thenReturn(2L);
        when(child.getName()).thenReturn("자식");
        when(child.getParent()).thenReturn(parent);

        List<Category> allCategories = List.of(parent, child);
        when(categoryRepository.findAll()).thenReturn(allCategories);

        // when
        List<CategoryListDto> categoryTree = categoryService.getCategoryTree();

        // then
        assertThat(categoryTree).hasSize(1);
        CategoryListDto parentDto = categoryTree.getFirst();
        assertThat(parentDto.id()).isEqualTo(1L);
        assertThat(parentDto.name()).isEqualTo("부모");
        assertThat(parentDto.children()).hasSize(1);

        CategoryListDto childDto = parentDto.children().getFirst();
        assertThat(childDto.id()).isEqualTo(2L);
        assertThat(childDto.name()).isEqualTo("자식");
        assertThat(childDto.children()).isEmpty();
    }

    @Test
    @DisplayName("createCategory - 루트 카테고리 생성 성공")
    void createCategory_Success_Root() {
        // given
        CategoryCreateDto dto = new CategoryCreateDto(null, "새 루트");
        when(categoryRepository.existsByNameAndParent(dto.name(), null)).thenReturn(false);

        // when
        categoryService.createCategory(dto);

        // then
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getParent()).isNull();
        assertThat(captor.getValue().getName()).isEqualTo("새 루트");
    }

    @Test
    @DisplayName("createCategory - 자식 카테고리 생성 성공")
    void createCategory_Success_Child() {
        // given
        Category parent = new Category(null, "부모");
        CategoryCreateDto dto = new CategoryCreateDto(1L, "새 자식");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.existsByNameAndParent(dto.name(), parent)).thenReturn(false);

        // when
        categoryService.createCategory(dto);

        // then
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getParent()).isEqualTo(parent);
        assertThat(captor.getValue().getName()).isEqualTo("새 자식");
    }

    @Test
    @DisplayName("createCategory - 유효하지 않은 부모 ID로 실패")
    void createCategory_Fail_InvalidParent() {
        // given
        CategoryCreateDto dto = new CategoryCreateDto(999L, "새 자식");
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.createCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARENT_CATEGORY_ID);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCategory - 중복된 이름으로 실패")
    void createCategory_Fail_DuplicateName() {
        // given
        CategoryCreateDto dto = new CategoryCreateDto(null, "중복 이름");
        when(categoryRepository.existsByNameAndParent("중복 이름", null)).thenReturn(true);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.createCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCategory - 카테고리 이름 변경 성공")
    void updateCategory_Success() {
        // given
        CategoryUpdateDto dto = new CategoryUpdateDto(1L, null, "새 이름");
        Category category = mock(Category.class);

        when(category.getName()).thenReturn("옛날 이름");
        when(category.getParent()).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndParent("새 이름", null)).thenReturn(false);

        // when
        categoryService.updateCategory(dto);

        // then
        verify(categoryRepository, times(1)).findById(1L);
        verify(category, times(1)).updateName("새 이름");
    }

    @Test
    @DisplayName("updateCategory - 존재하지 않는 카테고리로 실패")
    void updateCategory_Fail_NotFound() {
        // given
        CategoryUpdateDto dto = new CategoryUpdateDto(999L, null, "새 이름");
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.updateCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("updateCategory - 중복된 이름으로 실패")
    void updateCategory_Fail_DuplicateName() {
        // given
        CategoryUpdateDto dto = new CategoryUpdateDto(1L, null, "중복 이름");
        Category category = mock(Category.class);

        when(category.getName()).thenReturn("옛날 이름");
        when(category.getParent()).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndParent("중복 이름", null)).thenReturn(true);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.updateCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE);
        verify(category, never()).updateName(anyString());
    }

    @Test
    @DisplayName("deleteCategory - 카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        CategoryDeleteDto dto = new CategoryDeleteDto(1L);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // when
        categoryService.deleteCategory(dto);

        // then
        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteCategory - 존재하지 않는 카테고리로 실패")
    void deleteCategory_Fail_NotFound() {
        // given
        CategoryDeleteDto dto = new CategoryDeleteDto(999L);
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.deleteCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteCategory - 사용중인 카테고리 (무결성 제약)로 실패")
    void deleteCategory_Fail_InUse() {
        // given
        CategoryDeleteDto dto = new CategoryDeleteDto(1L);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("무결성 제약 위반"))
                .when(categoryRepository).deleteById(1L);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            categoryService.deleteCategory(dto);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_IN_USE);
    }
}