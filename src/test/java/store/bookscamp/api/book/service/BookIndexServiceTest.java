package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.entity.BookStatus;

class BookIndexServiceTest {

    private final ElasticsearchOperations esOps = mock(ElasticsearchOperations.class);

    private BookIndexService createService() {
        return new BookIndexService(esOps);
    }

    private void setId(Object target, Long id) {
        try {
            Field f = target.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("mapBookToDocument - Book 엔티티를 BookDocument로 매핑 성공")
    void mapBookToDocument_success() {
        BookIndexService service = createService();

        Book book = new Book(
                "제목",
                "설명",
                "목차",
                "출판사",
                LocalDate.of(2023, 1, 1),
                "1234567890123",
                "저자",
                BookStatus.AVAILABLE,
                true,
                20000,
                18000,
                50,
                0L
        );
        setId(book, 1L);

        BookDocument doc = service.mapBookToDocument(book);

        assertThat(doc.getId()).isEqualTo(1L);
        assertThat(doc.getTitle()).isEqualTo("제목");
        assertThat(doc.getPublisher()).isEqualTo("출판사");
        assertThat(doc.getIsbn()).isEqualTo("1234567890123");
        assertThat(doc.getRegularPrice()).isEqualTo(20000);
        assertThat(doc.getStatus()).isEqualTo(BookStatus.AVAILABLE.name());
        assertThat(doc.isPackable()).isTrue();
    }

    @Test
    @DisplayName("projectionToDoc - Projection → Document 매핑 성공")
    void projectionToDoc_success() {
        BookIndexService service = createService();

        BookProjection p = mock(BookProjection.class);
        when(p.getId()).thenReturn(77L);
        when(p.getTitle()).thenReturn("책");
        when(p.getExplanation()).thenReturn("설명");
        when(p.getContent()).thenReturn("내용");
        when(p.getPublisher()).thenReturn("출판");
        when(p.getCategory()).thenReturn("소설");
        when(p.getPublishDate()).thenReturn(LocalDate.of(2020, 3, 10));
        when(p.getIsbn()).thenReturn("9870001112223");
        when(p.getContributors()).thenReturn("저자A");
        when(p.getRegularPrice()).thenReturn(10000);
        when(p.getSalePrice()).thenReturn(9000);
        when(p.getStock()).thenReturn(10);
        when(p.getViewCount()).thenReturn(123L);
        when(p.getPackable()).thenReturn(true);
        when(p.getStatus()).thenReturn("AVAILABLE");
        when(p.getAverageRating()).thenReturn(4.7);
        when(p.getReviewCount()).thenReturn(8L);

        BookDocument doc = service.projectionToDoc(p);

        assertThat(doc.getId()).isEqualTo(77L);
        assertThat(doc.getCategory()).isEqualTo("소설");
        assertThat(doc.getSalePrice()).isEqualTo(9000);
        assertThat(doc.getReviewCount()).isEqualTo(8);
        assertThat(doc.getAverageRating()).isEqualTo(4.7);
        assertThat(doc.isPackable()).isTrue();
    }
    @Test
    @DisplayName("generateEmbedding - HTTP 실패 시 fallback 1024 길이 배열")
    void generateEmbedding_fallback() {
        BookIndexService service = createService();

        // 실제 Ollama 서버가 없어도, 예외 발생 → fallback 배열(길이 1024) 리턴
        float[] vec = service.generateEmbedding("test text");

        assertThat(vec).isNotNull();
        assertThat(vec.length).isEqualTo(1024);
    }

    @Test
    @DisplayName("indexBook - Elastic save 호출 확인")
    void indexBook_success() {
        BookIndexService service = createService();

        ElasticsearchOperations opsMock = mock(ElasticsearchOperations.class);
        when(esOps.withRefreshPolicy(RefreshPolicy.WAIT_UNTIL)).thenReturn(opsMock);

        BookDocument doc = new BookDocument();
        doc.setTitle("테스트책");

        service.indexBook(doc);

        verify(opsMock, times(1)).save(doc);
    }

    @Test
    @DisplayName("deleteBookIndex - Elastic delete 호출 확인")
    void deleteBook_success() {
        BookIndexService service = createService();

        ElasticsearchOperations opsMock = mock(ElasticsearchOperations.class);
        when(esOps.withRefreshPolicy(RefreshPolicy.IMMEDIATE)).thenReturn(opsMock);

        service.deleteBookIndex(10L);

        verify(opsMock, times(1)).delete("10", BookDocument.class);
    }
}
