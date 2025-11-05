package store.bookscamp.api.book.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.book.repository.custom.impl.BookRepositoryCustomImpl;
import store.bookscamp.api.bookcategory.entity.QBookCategory;
import store.bookscamp.api.booklike.entity.QBookLike;
import store.bookscamp.api.category.entity.QCategory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class BookRepositoryCustomImplTest {

    @InjectMocks
    private BookRepositoryCustomImpl bookRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    private static final QBook book = QBook.book;
    private static final QBookCategory bookCategory = QBookCategory.bookCategory;
    private static final QCategory category = QCategory.category;
    private static final QBookLike bookLike = QBookLike.bookLike;

    @Mock(answer = Answers.RETURNS_SELF)
    private JPAQuery<Book> bookQuery;

    @Mock(answer = Answers.RETURNS_SELF)
    private JPAQuery<Long> countQuery;

    @BeforeEach
    void setUp() {
        when(queryFactory.select(book)).thenReturn(bookQuery);
        when(queryFactory.select(book.countDistinct())).thenReturn(countQuery);
    }

    @Test
    @DisplayName("도서 목록 조회 - 카테고리O, 정렬O, 페이징O")
    void getBooks_WithCategoryAndSort_Success() {
        // Given
        List<Long> categoryIds = List.of(1L, 2L);
        String sortType = "title";
        Pageable pageable = PageRequest.of(0, 10);

        List<Book> mockResults = List.of(mock(Book.class));
        Long mockTotalCount = 15L;

        when(bookQuery.fetch()).thenReturn(mockResults);
        when(countQuery.fetchOne()).thenReturn(mockTotalCount);

        // When
        Page<Book> resultPage = bookRepository.getBooks(categoryIds, null, sortType, pageable);

        // Then
        assertNotNull(resultPage);
        assertEquals(mockTotalCount, resultPage.getTotalElements());
        assertEquals(mockResults, resultPage.getContent());
        assertEquals(2, resultPage.getTotalPages());
        assertEquals(0, resultPage.getNumber());

        verify(queryFactory).select(book);
        verify(bookQuery).where(category.id.in(categoryIds));
        verify(bookQuery).orderBy(book.title.asc());
        verify(bookQuery).offset(0L);
        verify(bookQuery).limit(10L);
        verify(bookQuery).fetch();

        verify(queryFactory).select(book.countDistinct());
        verify(countQuery).where(category.id.in(categoryIds));
        verify(countQuery).fetchOne();
    }

    @Test
    @DisplayName("도서 목록 조회 - 카테고리 null일 경우 where(null) 호출 검증")
    void getBooks_WithNullCategory_Success() {
        // Given
        List<Long> categoryIds = null;
        String sortType = "bookLike";
        Pageable pageable = PageRequest.of(1, 5);

        List<Book> mockResults = List.of();
        Long mockTotalCount = 3L;

        when(bookQuery.fetch()).thenReturn(mockResults);
        when(countQuery.fetchOne()).thenReturn(mockTotalCount);

        // When
        Page<Book> resultPage = bookRepository.getBooks(categoryIds, null, sortType, pageable);

        // Then
        assertEquals(mockTotalCount, resultPage.getTotalElements());
        assertEquals(1, resultPage.getTotalPages());
        assertEquals(1, resultPage.getNumber());

        verify(bookQuery).where((BooleanExpression) null);
        verify(bookQuery).orderBy(bookLike.id.count().desc());
        verify(bookQuery).offset(5L);
        verify(bookQuery).limit(5L);

        verify(countQuery).where((BooleanExpression) null);
    }

    @Test
    @DisplayName("도서 목록 조회 - 기본 정렬(default) 검증")
    void getBooks_WithDefaultSort_Success() {
        // Given
        String sortType = "invalidSortType";
        Pageable pageable = PageRequest.of(0, 10);

        when(bookQuery.fetch()).thenReturn(List.of());
        when(countQuery.fetchOne()).thenReturn(0L);

        // When
        bookRepository.getBooks(null, null, sortType, pageable);

        // Then
        verify(bookQuery).orderBy(book.id.asc());
    }

    @Test
    @DisplayName("도서 목록 조회 - 카운트 결과가 null일 경우 0L 반환 검증")
    void getBooks_WithNullTotalCount_ReturnsZero() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(bookQuery.fetch()).thenReturn(List.of());
        when(countQuery.fetchOne()).thenReturn(null);

        // When
        Page<Book> resultPage = bookRepository.getBooks(null, null, "title", pageable);

        // Then
        assertNotNull(resultPage);
        assertEquals(0L, resultPage.getTotalElements());
    }
}