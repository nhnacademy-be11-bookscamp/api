package store.bookscamp.api.book.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.booktag.repository.BookTagRepository;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.tag.repository.TagRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    @MockitoBean
    private BookImageRepository bookImageRepository;
    @MockitoBean
    private BookTagRepository bookTagRepository;
    @MockitoBean
    private BookImageService bookImageService;
    @MockitoBean
    private BookCategoryRepository bookCategoryRepository;

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

    @Test
    @DisplayName("updateBook - 책 정보 수정 성공")
    void updateBook_Success() {
        // given
        Long bookId = 1L;
        Book book = createMockBook(bookId, "JPA 책");

        BookUpdateRequest updateRequest = new BookUpdateRequest(
                "JPA 책 수정",                // 제목
                "저자 B",                     // 저자
                "출판사 B",                   // 출판사
                "1234567890124",              // ISBN
                LocalDate.now(),              // 출판일자
                42000,                        // 정가
                38000,                        // 판매가
                120,                          // 재고
                true,                         // 포장 가능 여부
                "새로운 내용",                 // 내용
                "새로운 설명",                 // 설명
                List.of(1L, 2L),              // 태그 ID
                1L,
                List.of("imageUrl1", "imageUrl2"), // 이미지 URL
                List.of("imageUrlToRemove1"), // 제거할 이미지 URL
                BookStatus.DISCONTINUED      // 상태 (UNAVAILABLE 대신 DISCONTINUED)
        );

        when(bookRepository.findById(bookId)).thenReturn(java.util.Optional.of(book));

        // when
        bookService.updateBook(bookId, updateRequest);

        // then
        assertThat(book.getTitle()).isEqualTo("JPA 책 수정");
        assertThat(book.getContributors()).isEqualTo("저자 B");
        assertThat(book.getPublisher()).isEqualTo("출판사 B");
        assertThat(book.getIsbn()).isEqualTo("1234567890124");
        assertThat(book.getRegularPrice()).isEqualTo(42000);
        assertThat(book.getSalePrice()).isEqualTo(38000);
        assertThat(book.getStock()).isEqualTo(120);
        assertThat(book.getStatus()).isEqualTo(BookStatus.DISCONTINUED);  // 상태 확인
        assertThat(book.getContent()).isEqualTo("새로운 내용");
        assertThat(book.getExplanation()).isEqualTo("새로운 설명");

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookCategoryRepository, times(1)).deleteByBook(book);
        verify(bookTagRepository, times(1)).deleteByBook(book);
        verify(bookImageService, times(1)).createBookImage(any());
    }

    @Test
    @DisplayName("updateBook - 책이 없으면 BOOK_NOT_FOUND 예외 발생")
    void updateBook_Fail_BookNotFound() {
        // given
        Long nonExistentBookId = 999L;
        BookUpdateRequest updateRequest = new BookUpdateRequest(
                "책 제목",
                "저자 A",
                "출판사 A",
                "1234567890123",
                LocalDate.now(),
                40000,
                35000,
                100,
                true,
                "책 내용",
                "책 설명",
                List.of(1L),
                1L,
                List.of("imageUrl1"),
                List.of("imageUrlToRemove1"),
                BookStatus.AVAILABLE  // AVAILABLE 상태
        );

        when(bookRepository.findById(nonExistentBookId)).thenReturn(java.util.Optional.empty());

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookService.updateBook(nonExistentBookId, updateRequest);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        verify(bookRepository, times(1)).findById(nonExistentBookId);
    }

    private Book createMockBook(Long id, String title) {
        Book mockBook = mock(Book.class);
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
