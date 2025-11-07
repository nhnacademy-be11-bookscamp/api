package store.bookscamp.api.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import store.bookscamp.api.category.controller.request.CategoryCreateRequest;
import store.bookscamp.api.category.controller.request.CategoryUpdateRequest;
import store.bookscamp.api.category.service.CategoryService;
import store.bookscamp.api.category.service.dto.CategoryCreateDto;
import store.bookscamp.api.category.service.dto.CategoryDeleteDto;
import store.bookscamp.api.category.service.dto.CategoryListDto;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /categories - 카테고리 트리 조회 성공")
    void getCategoryTree_Success() throws Exception {
        // given
        CategoryListDto childDto = new CategoryListDto(2L, "자식 카테고리", List.of());
        CategoryListDto parentDto = new CategoryListDto(1L, "부모 카테고리", List.of(childDto));
        List<CategoryListDto> dtoList = List.of(parentDto);

        when(categoryService.getCategoryTree()).thenReturn(dtoList);

        // when
        ResultActions result = mockMvc.perform(get("/categories")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("부모 카테고리"))
                .andExpect(jsonPath("$[0].children[0].id").value(2L))
                .andExpect(jsonPath("$[0].children[0].name").value("자식 카테고리"))
                .andExpect(jsonPath("$[0].children[0].children").isEmpty());

        verify(categoryService, times(1)).getCategoryTree();
    }

    @Test
    @DisplayName("POST /admin/category/create - 카테고리 생성 성공")
    void createCategory_Success() throws Exception {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest(1L, "새 카테고리");
        doNothing().when(categoryService).createCategory(any(CategoryCreateDto.class));

        // when
        ResultActions result = mockMvc.perform(post("/admin/category/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated());

        ArgumentCaptor<CategoryCreateDto> captor = ArgumentCaptor.forClass(CategoryCreateDto.class);
        verify(categoryService, times(1)).createCategory(captor.capture());

        CategoryCreateDto capturedDto = captor.getValue();
        assertThat(capturedDto.parentId()).isEqualTo(1L);
        assertThat(capturedDto.name()).isEqualTo("새 카테고리");
    }

    @Test
    @DisplayName("PUT /admin/category/update/{id} - 카테고리 수정 성공")
    void updateCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest(null, "수정된 이름");

        doNothing().when(categoryService).updateCategory(any(CategoryUpdateDto.class));

        // when
        ResultActions result = mockMvc.perform(put("/admin/category/update/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        ArgumentCaptor<CategoryUpdateDto> captor = ArgumentCaptor.forClass(CategoryUpdateDto.class);
        verify(categoryService, times(1)).updateCategory(captor.capture());

        CategoryUpdateDto capturedDto = captor.getValue();
        assertThat(capturedDto.id()).isEqualTo(categoryId);
        assertThat(capturedDto.parent()).isNull();
        assertThat(capturedDto.name()).isEqualTo("수정된 이름");
    }

    @Test
    @DisplayName("DELETE /admin/category/delete/{id} - 카테고리 삭제 성공")
    void deleteCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategory(any(CategoryDeleteDto.class));

        // when
        ResultActions result = mockMvc.perform(delete("/admin/category/delete/{id}", categoryId));

        // then
        result.andExpect(status().isOk());

        ArgumentCaptor<CategoryDeleteDto> captor = ArgumentCaptor.forClass(CategoryDeleteDto.class);
        verify(categoryService, times(1)).deleteCategory(captor.capture());

        CategoryDeleteDto capturedDto = captor.getValue();
        assertThat(capturedDto.id()).isEqualTo(categoryId);
    }
}