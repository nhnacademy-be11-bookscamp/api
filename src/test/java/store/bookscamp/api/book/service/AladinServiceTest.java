package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.common.exception.ApplicationException;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "aladin.ttb-key=TEST",
        "aladin.output=json",
        "aladin.version=1"
})
class AladinServiceTest {

    @Autowired
    private AladinService service;

    private LocalDate invokeParse(String s) {
        try {
            Method m = AladinService.class.getDeclaredMethod("parseDate", String.class);
            m.setAccessible(true);
            return (LocalDate) m.invoke(service, s);
        } catch (Exception e) {
            if (e.getCause() instanceof ApplicationException app) {
                throw app;
            }
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("parseDate - yyyy-MM-dd")
    void parseDate_full() {
        LocalDate date = invokeParse("2024-01-05");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("parseDate - yyyy-MM (자동 1일 처리)")
    void parseDate_yearMonth() {
        LocalDate date = invokeParse("2024-01");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("parseDate - BASIC yyyyMMdd")
    void parseDate_basic() {
        LocalDate date = invokeParse("20240105");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("parseDate - null 또는 빈 문자열 → 오늘 날짜")
    void parseDate_nullOrEmpty() {
        assertThat(invokeParse(null)).isEqualTo(LocalDate.now());
        assertThat(invokeParse("")).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("parseDate - 잘못된 형식은 오늘 날짜 반환")
    void parseDate_error() {
        LocalDate result = invokeParse("invalid-date");
        assertThat(result).isEqualTo(LocalDate.now());
    }

    private int invokeDefaultInt(Integer v, int d) {
        try {
            Method m = AladinService.class.getDeclaredMethod("defaultInt", Integer.class, int.class);
            m.setAccessible(true);
            return (int) m.invoke(service, v, d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("defaultInt - null -> default 사용")
    void defaultInt_null() {
        assertThat(invokeDefaultInt(null, 5)).isEqualTo(5);
    }

    @Test
    @DisplayName("defaultInt - 값 존재")
    void defaultInt_value() {
        assertThat(invokeDefaultInt(10, 5)).isEqualTo(10);
    }

    @Test
    @DisplayName("toBookEntity - 모든 매핑 정상 작동")
    void toBookEntity_success() {

        AladinItem item = new AladinItem();
        item.setTitle("테스트서적");
        item.setDescription("설명");
        item.setToc("목차");
        item.setPublisher("출판사");
        item.setIsbn13("9781111222233");
        item.setPubDate("2024-01-10");
        item.setPriceStandard(20000);
        item.setPriceSales(18000);

        Book book = service.toBookEntity(item, "저자A", BookStatus.AVAILABLE, true);

        assertThat(book.getTitle()).isEqualTo("테스트서적");
        assertThat(book.getExplanation()).isEqualTo("설명");
        assertThat(book.getContent()).isEqualTo("목차");
        assertThat(book.getPublisher()).isEqualTo("출판사");
        assertThat(book.getIsbn()).isEqualTo("9781111222233");

        assertThat(book.getRegularPrice()).isEqualTo(20000);
        assertThat(book.getSalePrice()).isEqualTo(18000);
        assertThat(book.getPublishDate()).isEqualTo(LocalDate.of(2024, 1, 10));

        assertThat(book.isPackable()).isTrue();
        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(book.getViewCount()).isEqualTo(0L);
    }
}
