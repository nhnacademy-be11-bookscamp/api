package store.bookscamp.api.bookimage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.common.config.JpaConfig;

@Import(JpaConfig.class)
@DataJpaTest
class BookImageRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookImageRepository bookImageRepository;

    @Test
    @DisplayName("bookId로 썸네일(true) 이미지 조회 성공")
    void findByBookIdAndIsThumbnailTrue_success() {
        // given
        Book book = bookRepository.save(new Book(
                "도서1",
                "설명",
                null,
                "출판사",
                LocalDate.of(2000, 1, 1),
                "1234567890",
                "저자",
                BookStatus.AVAILABLE,
                false,
                10000,
                9000,
                10,
                0L
        ));

        BookImage thumbnail = new BookImage(book, "thumb.jpg", true);
        BookImage normal = new BookImage(book, "normal.jpg", false);

        bookImageRepository.save(thumbnail);
        bookImageRepository.save(normal);

        // when
        BookImage result =
                bookImageRepository.findByBookIdAndIsThumbnailTrue(book.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.isThumbnail()).isTrue();
        assertThat(result.getImageUrl()).isEqualTo("thumb.jpg");
        assertThat(result.getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    @DisplayName("썸네일이 없는 경우 null 반환")
    void findByBookIdAndIsThumbnailTrue_noThumbnail() {
        // given
        Book book = bookRepository.save(new Book(
                "도서2",
                "설명2",
                null,
                "출판사2",
                LocalDate.of(2010, 5, 10),
                "9876543210",
                "저자2",
                BookStatus.AVAILABLE,
                false,
                20000,
                18000,
                5,
                0L
        ));

        BookImage normal = new BookImage(book, "normal.jpg", false);
        bookImageRepository.save(normal);

        // when
        BookImage result =
                bookImageRepository.findByBookIdAndIsThumbnailTrue(book.getId());

        // then
        assertThat(result).isNull();
    }
}
