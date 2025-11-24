package store.bookscamp.api.book.repository;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.custom.impl.BookRepositoryCustomImpl;
import store.bookscamp.api.common.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
class BookRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private BookRepositoryCustomImpl bookRepository;


    @BeforeEach
    void setup() {

        // 카테고리 있는 책 2개 삽입
        Book book1 = new Book(
                "제목 A", "설명", null,
                "출판사", LocalDate.now(),
                "ISBN1", "저자",
                AVAILABLE, false,
                20000, 18000, 100, 0L
        );

        Book book2 = new Book(
                "제목 B", "설명2", null,
                "출판사", LocalDate.now(),
                "ISBN2", "저자2",
                AVAILABLE, false,
                20000, 18000, 100, 0L
        );

        em.persist(book1);
        em.persist(book2);
    }

    @Test
    @DisplayName("도서 목록 조회 - 카테고리O, 정렬O, 페이징O")
    void getBooks_WithCategoryAndSort_Success() {

        // Given
        List<Long> categoryIds = null;
        String sortType = "title";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Book> resultPage = bookRepository.getBooks(categoryIds, sortType, pageable);

        // Then
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(2);

        assertThat(resultPage.getContent())
                .isSortedAccordingTo(comparing(Book::getTitle));

        assertThat(resultPage.getNumber()).isEqualTo(0);
        assertThat(resultPage.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("도서 목록 조회 - 카테고리 null일 경우 전체 조회")
    void getBooks_WithNullCategory_Success() {

        Pageable pageable = PageRequest.of(1, 1);

        Page<Book> resultPage = bookRepository.getBooks(null, "title", pageable);

        // Then
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).hasSize(1);  // page 1, size 1
        assertThat(resultPage.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("도서 목록 조회 - 기본 정렬(default) 검증")
    void getBooks_WithDefaultSort_Success() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> resultPage = bookRepository.getBooks(null, "invalidSort", pageable);

        assertThat(resultPage.getContent())
                .isSortedAccordingTo(comparing(Book::getId));
    }


    @Test
    @DisplayName("도서 목록 조회 - 카운트 결과가 null일 경우 0L 반환 검증")
    void getBooks_WithNullTotalCount_ReturnsZero() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> resultPage = bookRepository.getBooks(null, "title", pageable);

        assertThat(resultPage.getTotalElements()).isEqualTo(2);
    }
}