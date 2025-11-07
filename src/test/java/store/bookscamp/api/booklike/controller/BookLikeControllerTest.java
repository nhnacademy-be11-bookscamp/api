package store.bookscamp.api.booklike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import store.bookscamp.api.booklike.controller.request.BookLikeRequest;
import store.bookscamp.api.booklike.service.BookLikeService;
import store.bookscamp.api.booklike.service.dto.BookLikeCountDto;
import store.bookscamp.api.booklike.service.dto.BookLikeStatusDto;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(controllers = BookLikeController.class)
class BookLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookLikeService bookLikeService;

    private final Long hardcodedMemberId = 4L;

    @Test
    @DisplayName("PUT /books/like/{bookId} - 좋아요 토글 성공")
    void toggleLike_Success() throws Exception {
        // given
        Long bookId = 10L;
        boolean isLiked = true;
        BookLikeRequest request = new BookLikeRequest(isLiked);

        // void 메서드 모킹
        doNothing().when(bookLikeService).toggleLike(hardcodedMemberId, bookId, isLiked);

        // when
        ResultActions result = mockMvc.perform(put("/books/like/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        // 컨트롤러에서 하드코딩된 4L로 호출되었는지 검증
        verify(bookLikeService, times(1)).toggleLike(hardcodedMemberId, bookId, isLiked);
    }

    @Test
    @DisplayName("GET /books/{bookId}/like/count - 좋아요 개수 조회 성공")
    void getLikeCount_Success() throws Exception {
        // given
        Long bookId = 10L;
        Long count = 123L;
        BookLikeCountDto dto = new BookLikeCountDto(count);

        when(bookLikeService.getLikeCount(bookId)).thenReturn(dto);

        // when
        ResultActions result = mockMvc.perform(get("/books/{bookId}/like/count", bookId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(count));

        verify(bookLikeService, times(1)).getLikeCount(bookId);
    }

    @Test
    @DisplayName("GET /books/{bookId}/like/status - 좋아요 상태 조회 성공 (true)")
    void getLikeStatus_Success_True() throws Exception {
        // given
        Long bookId = 10L;
        boolean status = true;
        BookLikeStatusDto dto = new BookLikeStatusDto(status);

        when(bookLikeService.getLikeStatus(hardcodedMemberId, bookId)).thenReturn(dto);

        // when
        ResultActions result = mockMvc.perform(get("/books/{bookId}/like/status", bookId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));

        // 컨트롤러에서 하드코딩된 4L로 호출되었는지 검증
        verify(bookLikeService, times(1)).getLikeStatus(hardcodedMemberId, bookId);
    }

    @Test
    @DisplayName("GET /books/{bookId}/like/status - 좋아요 상태 조회 성공 (false)")
    void getLikeStatus_Success_False() throws Exception {
        // given
        Long bookId = 10L;
        boolean status = false;
        BookLikeStatusDto dto = new BookLikeStatusDto(status);

        when(bookLikeService.getLikeStatus(hardcodedMemberId, bookId)).thenReturn(dto);

        // when
        ResultActions result = mockMvc.perform(get("/books/{bookId}/like/status", bookId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));

        verify(bookLikeService, times(1)).getLikeStatus(hardcodedMemberId, bookId);
    }
}