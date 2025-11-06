package store.bookscamp.api.book.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(controllers = BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("GET /books - 도서 목록 조회 (페이징) 성공")
    void getBooks_Success() throws Exception {
        // given
        BookSortDto book1 = new BookSortDto(
                1L, "JPA 책", "JPA 설명", "JPA 내용", "북스캠프", LocalDate.now(),
                "저자 A", BookStatus.AVAILABLE, true, 40000, 36000, 100, 50L
        );
        BookSortDto book2 = new BookSortDto(
                2L, "Spring 책", "Spring 설명", "Spring 내용", "북스캠프", LocalDate.now(),
                "저자 B", BookStatus.AVAILABLE, true, 45000, 40000, 100, 120L
        );
        List<BookSortDto> dtoList = List.of(book1, book2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookSortDto> dtoPage = new PageImpl<>(dtoList, pageable, dtoList.size());

        when(bookService.searchBooks(any(), any(), anyString(), any(Pageable.class)))
                .thenReturn(dtoPage);

        // when & then
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortType", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value(book1.title()))
                .andExpect(jsonPath("$.content[0].salePrice").value(book1.salePrice()));

        verify(bookService, times(1))
                .searchBooks(any(), any(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /bookDetail/{id} - 도서 상세 조회 성공")
    void getBookDetail_Success() throws Exception {
        // given
        long bookId = 1L;
        BookDetailDto bookDetail = new BookDetailDto(
                bookId, "JPA 책 상세", "JPA 상세 설명", "JPA 상세 내용", "북스캠프", LocalDate.now(),
                "저자 A, 역자 C", BookStatus.AVAILABLE, true, 40000, 36000, 100, 150L
        );

        when(bookService.getBookDetail(bookId)).thenReturn(bookDetail);

        // when & then
        mockMvc.perform(get("/bookDetail/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(bookDetail.title()))
                .andExpect(jsonPath("$.regularPrice").value(bookDetail.regularPrice()))
                .andExpect(jsonPath("$.publisher").value(bookDetail.publisher()));

        verify(bookService, times(1)).getBookDetail(bookId);
    }

    @Test
    @DisplayName("GET /bookDetail/{id} - 도서 상세 조회 실패 (도서 없음)")
    void getBookDetail_Fail_BookNotFound() throws Exception {
        // given
        long nonExistentBookId = 999L;

        when(bookService.getBookDetail(nonExistentBookId))
                .thenThrow(new ApplicationException(ErrorCode.BOOK_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/bookDetail/{id}", nonExistentBookId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorCode.BOOK_NOT_FOUND.getMessage()));

        verify(bookService, times(1)).getBookDetail(nonExistentBookId);
    }
}