package store.bookscamp.api.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;

@SpringBootTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = bookRepository.save(new Book(
                "도서1",
                "설명",
                null,
                "출판사",
                LocalDate.of(2000, 1, 1),
                "1234567890123",
                "저자",
                BookStatus.AVAILABLE,
                false,
                10000,
                9000,
                10,
                0L
        ));

        book2 = bookRepository.save(new Book(
                "도서2",
                "설명2",
                null,
                "출판사2",
                LocalDate.of(2000, 1, 1),
                "1234567891123",
                "저자",
                BookStatus.AVAILABLE,
                false,
                20000,
                18000,
                20,
                0L
        ));
    }

    @Test
    @DisplayName("findById - 정상적으로 도서 조회")
    void findById_success() {
        // given
        Long bookId = book1.getId();

        // when
        Optional<Book> foundBook = bookRepository.findById(bookId);

        // then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("도서1");
    }

    @Test
    @DisplayName("findById - 존재하지 않는 도서 조회")
    void findById_notFound() {
        // given
        Long invalidBookId = -1L;

        // when
        Optional<Book> foundBook = bookRepository.findById(invalidBookId);

        // then
        assertThat(foundBook).isNotPresent();
    }

    @Test
    @DisplayName("findById - 정상적인 도서 조회 후 다른 도서 확인")
    void findById_checkOtherBook() {
        // given
        Long bookId = book2.getId();

        // when
        Optional<Book> foundBook = bookRepository.findById(bookId);

        // then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("도서2");
    }

    @Test
    @DisplayName("findById - 잘못된 ID로 도서 조회시 예외 처리 확인")
    void findById_invalidId() {
        // given
        Long invalidId = 9999L;

        // when
        Optional<Book> foundBook = bookRepository.findById(invalidId);

        // then
        assertThat(foundBook).isNotPresent();
    }
}
