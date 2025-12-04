package store.bookscamp.api.book.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.book.entity.BookStatus.SOLD_OUT;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

class BookTest {

    private Book createBook() {
        return new Book(
                "title",
                "explanation",
                "content",
                "publisher",
                LocalDate.of(2024, 1, 1),
                "1234567890123",
                "contributors",
                AVAILABLE,
                true,
                20000,
                15000,
                10,
                0
        );
    }

    @Test
    @DisplayName("increaseViewCount - 조회수 증가 성공")
    void increaseViewCount_success() {
        Book book = createBook();

        book.increaseViewCount();
        book.increaseViewCount();

        assertThat(book.getViewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("setStatus - 판매 상태 변경 성공")
    void setStatus_success() {
        Book book = createBook();

        book.setStatus(SOLD_OUT);

        assertThat(book.getStatus()).isEqualTo(SOLD_OUT);
    }

    @Test
    @DisplayName("decreaseStock - 재고 차감 성공")
    void decreaseStock_success() {
        Book book = createBook();

        book.decreaseStock(3);

        assertThat(book.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("decreaseStock - 재고 부족 시 예외 발생")
    void decreaseStock_fail_insufficient() {
        Book book = createBook();

        assertThatThrownBy(() -> book.decreaseStock(20))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_STOCK.getMessage());
    }

    @Test
    @DisplayName("increaseStock - 재고 증가 성공")
    void increaseStock_success() {
        Book book = createBook();

        book.increaseStock(5);

        assertThat(book.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("updateInfo - 정보 수정 성공")
    void updateInfo_success() {
        Book book = createBook();

        book.updateInfo(
                "newTitle",
                "newContributors",
                "newPublisher",
                "9988776655443",
                LocalDate.of(2023, 12, 12),
                30000,
                25000,
                50,
                false,
                "newContent",
                "newExplanation"
        );

        assertThat(book.getTitle()).isEqualTo("newTitle");
        assertThat(book.getContributors()).isEqualTo("newContributors");
        assertThat(book.getPublisher()).isEqualTo("newPublisher");
        assertThat(book.getIsbn()).isEqualTo("9988776655443");
        assertThat(book.getPublishDate()).isEqualTo(LocalDate.of(2023, 12, 12));
        assertThat(book.getRegularPrice()).isEqualTo(30000);
        assertThat(book.getSalePrice()).isEqualTo(25000);
        assertThat(book.getStock()).isEqualTo(50);
        assertThat(book.isPackable()).isFalse();
        assertThat(book.getContent()).isEqualTo("newContent");
        assertThat(book.getExplanation()).isEqualTo("newExplanation");
    }

    @Test
    @DisplayName("setAiReview - AI 리뷰 저장 성공")
    void setAiReview_success() {
        Book book = createBook();

        book.setAiReview("great book");

        assertThat(book.getAiReview()).isEqualTo("great book");
    }
}
