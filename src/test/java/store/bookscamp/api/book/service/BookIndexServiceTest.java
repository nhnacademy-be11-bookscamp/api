package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.test.context.ActiveProfiles;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
class BookIndexServiceTest {

    private final ElasticsearchOperations esOps = mock(ElasticsearchOperations.class);
    private final ElasticsearchClient esClient = mock(ElasticsearchClient.class);
    private final BookRepository bookRepository = mock(BookRepository.class);

    private BookIndexService createService() {
        BookIndexService service =
                new BookIndexService(esOps, esClient, bookRepository);

        setField(service, "INDEX_NAME", "bookscamp-test");

        return service;
    }

    private void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    @DisplayName("init 성공")
    void init_simple_mocking() throws Exception {
        BookIndexService service = createService();
        when(esClient.indices()).thenThrow(new RuntimeException("ignored"));
        when(bookRepository.findAllBooksWithRatingAndReview()).thenReturn(List.of());

        assertDoesNotThrow(() -> service.init());
    }



    @Test
    @DisplayName("mapBookToDocument 성공")
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
    }

    @Test
    @DisplayName("projectionToDoc 필드 매핑 성공")
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
    }

    @Test
    @DisplayName("convertDocumentToMap 성공")
    void convertDocumentToMap_success() throws Exception {
        BookIndexService service = createService();

        BookDocument doc = BookDocument.builder()
                .id(1L)
                .title("책")
                .publisher("출판")
                .isbn("123")
                .publishDate(LocalDate.of(2021, 5, 1))
                .contributors("저자")
                .explanation("설명")
                .regularPrice(15000)
                .salePrice(12000)
                .stock(30)
                .status("AVAILABLE")
                .build();

        Method m = BookIndexService.class.getDeclaredMethod("convertDocumentToMap", BookDocument.class);
        m.setAccessible(true);

        Map<String, Object> map = (Map<String, Object>) m.invoke(service, doc);

        assertThat(map.get("id")).isEqualTo(1L);
        assertThat(map.get("title")).isEqualTo("책");
        assertThat(map.get("publishDate")).isEqualTo("2021-05-01");
        assertThat(map.get("regularPrice")).isEqualTo(15000);
    }

    @Test
    @DisplayName("generateEmbedding - HTTP 실패시 fallback 1024 길이 배열")
    void generateEmbedding_fallback() throws Exception {
        BookIndexService service = createService();

        Method m = BookIndexService.class.getDeclaredMethod("generateEmbedding", String.class);
        m.setAccessible(true);

        float[] vec = (float[]) m.invoke(service, "test text");

        assertThat(vec.length).isEqualTo(1024);
    }

    @Test
    @DisplayName("indexBook() - Elastic save 호출 확인")
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
    @DisplayName("deleteBookIndex() - Elastic delete 호출 확인")
    void deleteBook_success() {
        BookIndexService service = createService();

        ElasticsearchOperations opsMock = mock(ElasticsearchOperations.class);
        when(esOps.withRefreshPolicy(RefreshPolicy.IMMEDIATE)).thenReturn(opsMock);

        service.deleteBookIndex(10L);

        verify(opsMock, times(1)).delete("10", BookDocument.class);
    }
}
