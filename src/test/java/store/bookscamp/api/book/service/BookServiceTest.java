package store.bookscamp.api.book.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.tag.repository.TagRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;
    @MockitoBean
    private CategoryRepository categoryRepository;
    @MockitoBean
    private TagRepository tagRepository;

    @Test
    @DisplayName("searchBooks - categoryId가 null일 때 전체 검색")
    void searchBooks_withNullCategory() {
        // given
        Long categoryId = null;
        String keyword = "JPA";
        String sortType = "id";
        Pageable pageable = PageRequest.of(0, 10);

        Book book = createMockBook(1L, "JPA 책");
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.getBooks(null, keyword, sortType, pageable))
                .thenReturn(bookPage);

        // when
        Page<BookSortDto> resultPage = bookService.searchBooks(categoryId, keyword, sortType, pageable);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).title()).isEqualTo("JPA 책");

        verify(categoryRepository, never()).getAllDescendantIdsIncludingSelf(anyLong());
        verify(bookRepository, times(1)).getBooks(null, keyword, sortType, pageable);
    }

    @Test
    @DisplayName("searchBooks - categoryId가 있을 때 하위 카테고리 포함 검색")
    void searchBooks_withCategoryId() {
        // given
        Long categoryId = 1L;
        String keyword = "Spring";
        String sortType = "id";
        Pageable pageable = PageRequest.of(0, 10);
        List<Long> descendantIds = List.of(1L, 10L, 11L);

        Book book = createMockBook(2L, "Spring 책");
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(categoryRepository.getAllDescendantIdsIncludingSelf(categoryId))
                .thenReturn(descendantIds);
        when(bookRepository.getBooks(descendantIds, keyword, sortType, pageable))
                .thenReturn(bookPage);

        // when
        Page<BookSortDto> resultPage = bookService.searchBooks(categoryId, keyword, sortType, pageable);

        // then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).title()).isEqualTo("Spring 책");

        verify(categoryRepository, times(1)).getAllDescendantIdsIncludingSelf(categoryId);
        verify(bookRepository, times(1)).getBooks(descendantIds, keyword, sortType, pageable);
    }

    @Test
    @DisplayName("getBookDetail - 책 상세 조회 성공")
    void getBookDetail_Success() {
        // given
        Long bookId = 1L;
        Book book = createMockBook(bookId, "JPA 책 상세");

        when(bookRepository.getBookById(bookId)).thenReturn(book);

        // when
        BookDetailDto bookDetail = bookService.getBookDetail(bookId);

        // then
        assertThat(bookDetail).isNotNull();
        assertThat(bookDetail.id()).isEqualTo(bookId);
        assertThat(bookDetail.title()).isEqualTo("JPA 책 상세");
        assertThat(bookDetail.status()).isEqualTo(BookStatus.AVAILABLE);

        verify(bookRepository, times(1)).getBookById(bookId);
    }

    @Test
    @DisplayName("getBookDetail - 책이 없으면 BOOK_NOT_FOUND 예외 발생")
    void getBookDetail_Fail_BookNotFound() {
        // given
        Long nonExistentBookId = 999L;

        when(bookRepository.getBookById(nonExistentBookId)).thenReturn(null);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookService.getBookDetail(nonExistentBookId);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        verify(bookRepository, times(1)).getBookById(nonExistentBookId);
    }


    private Book createMockBook(Long id, String title) {
        Book mockBook = org.mockito.Mockito.mock(Book.class);
        when(mockBook.getId()).thenReturn(id);
        when(mockBook.getTitle()).thenReturn(title);
        when(mockBook.getExplanation()).thenReturn(title + " 설명");
        when(mockBook.getContent()).thenReturn(title + " 내용");
        when(mockBook.getPublisher()).thenReturn("북스캠프");
        when(mockBook.getPublishDate()).thenReturn(LocalDate.now());
        when(mockBook.getContributors()).thenReturn("저자 A");
        when(mockBook.getStatus()).thenReturn(BookStatus.AVAILABLE);
        when(mockBook.isPackable()).thenReturn(true);
        when(mockBook.getRegularPrice()).thenReturn(40000);
        when(mockBook.getSalePrice()).thenReturn(36000);
        when(mockBook.getStock()).thenReturn(100);
        when(mockBook.getViewCount()).thenReturn(50L);
        return mockBook;
    }
}