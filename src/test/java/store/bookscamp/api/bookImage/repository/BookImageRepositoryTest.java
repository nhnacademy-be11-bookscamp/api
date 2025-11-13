package store.bookscamp.api.bookImage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.querydsl.jpa.impl.JPAQueryFactory;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.common.config.JpaConfig;

@Import(JpaConfig.class)
@DataJpaTest
class BookImageRepositoryTest {

    @MockitoBean
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private BookImageRepository bookImageRepository;

    @Autowired
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    void setUp() {
        book = bookRepository.save(new Book(
                "테스트 도서",
                "도서 설명",
                "내용 요약",
                "테스트출판사",
                LocalDate.of(2024, 5, 1),
                "1234567890123",
                "테스트 저자",
                BookStatus.AVAILABLE,
                true,
                15000,
                13500,
                50,
                0L
        ));
    }

    @Test
    @DisplayName("findAllByBook - 특정 도서의 모든 이미지 조회")
    void findAllByBook_success() {
        // given
        BookImage img1 = bookImageRepository.save(new BookImage(book, "http://storage.test.net/book1.png", false));
        BookImage img2 = bookImageRepository.save(new BookImage(book, "http://storage.test.net/book2.png", true));

        // when
        List<BookImage> images = bookImageRepository.findAllByBook(book);

        // then
        assertThat(images).hasSize(2);
        assertThat(images)
                .extracting(BookImage::getImageUrl)
                .containsExactlyInAnyOrder(
                        "http://storage.test.net/book1.png",
                        "http://storage.test.net/book2.png"
                );
    }

    @Test
    @DisplayName("findByImageUrl - 이미지 URL로 단일 이미지 조회")
    void findByImageUrl_success() {
        // given
        BookImage img = bookImageRepository.save(new BookImage(book, "http://storage.test.net/book-image.png", true));

        // when
        Optional<BookImage> found = bookImageRepository.findByImageUrl("http://storage.test.net/book-image.png");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getBook().getId()).isEqualTo(book.getId());
        assertThat(found.get().getImageUrl()).isEqualTo("http://storage.test.net/book-image.png");
    }

    @Test
    @DisplayName("findByImageUrl - 존재하지 않는 이미지 URL 조회 시 빈 결과 반환")
    void findByImageUrl_notFound() {
        // when
        Optional<BookImage> found = bookImageRepository.findByImageUrl("http://storage.test.net/non-exist.png");

        // then
        assertThat(found).isNotPresent();
    }
}
