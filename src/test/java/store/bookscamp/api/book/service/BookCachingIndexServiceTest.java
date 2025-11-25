package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.*;

import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import store.bookscamp.api.book.entity.BookCaching;
import store.bookscamp.api.book.repository.BookCachingRepository;
import store.bookscamp.api.book.service.dto.BookSortDto;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class BookCachingIndexServiceTest {

    private final ElasticsearchClient esClient = mock(ElasticsearchClient.class);
    private final BookCachingRepository cacheRepo = mock(BookCachingRepository.class);
    private final ElasticsearchOperations esOps = mock(ElasticsearchOperations.class);

    private BookCachingIndexService createService() {
        BookCachingIndexService service =
                new BookCachingIndexService(esClient, cacheRepo, esOps);

        setField(service, "CACHING_INDEX", "bookscamp-caching-test");
        return service;
    }

    private void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("init() - ES 호출 중 exception 발생해도 정상 종료됨")
    void init_fallback() throws Exception {

        BookCachingIndexService service = createService();

        when(esClient.indices()).thenThrow(new RuntimeException("ignored"));

        assertDoesNotThrow(service::init);
    }

    @Test
    @DisplayName("getCache - 캐시 존재하고 TTL 안 지났을 때 반환됨")
    void getCache_ok() {

        BookCachingIndexService service = createService();

        BookCaching cache = BookCaching.builder()
                .keyword("java")
                .cachedAt(System.currentTimeMillis()) // TTL 안 지남
                .books(List.of())
                .build();

        when(cacheRepo.findById("java")).thenReturn(Optional.of(cache));

        Optional<BookCaching> result = service.getCache("java");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getCache - 캐시 TTL 만료 → 삭제 후 empty 반환")
    void getCache_expired() {

        BookCachingIndexService service = createService();

        BookCaching cache = BookCaching.builder()
                .keyword("java")
                .cachedAt(System.currentTimeMillis() - (2 * 60 * 60 * 1000)) // TTL 1시간 초과
                .books(List.of())
                .build();

        when(cacheRepo.findById("java")).thenReturn(Optional.of(cache));

        Optional<BookCaching> result = service.getCache("java");

        assertThat(result).isEmpty();
        verify(cacheRepo, times(1)).deleteById("java");
    }

    @Test
    @DisplayName("saveCache - 저장 성공")
    void saveCache_ok() {

        BookCachingIndexService service = createService();

        List<BookSortDto> books = List.of();
        service.saveCache("test", books);

        verify(cacheRepo, times(1)).save(any(BookCaching.class));
    }

    @Test
    @DisplayName("invalidateCachesContainingBook - 검색된 hit들 삭제됨")
    void invalidate_ok() {

        BookCachingIndexService service = createService();

        SearchHit<BookCaching> hit1 = Mockito.mock(SearchHit.class);
        when(hit1.getId()).thenReturn("key1");

        SearchHit<BookCaching> hit2 = Mockito.mock(SearchHit.class);
        when(hit2.getId()).thenReturn("key2");

        SearchHits<BookCaching> hits = Mockito.mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(hit1, hit2));

        when(esOps.search((Query) any(), eq(BookCaching.class)))
                .thenReturn(hits);

        service.invalidateCachesContainingBook(99L);

        verify(esOps, times(1)).delete("key1", BookCaching.class);
        verify(esOps, times(1)).delete("key2", BookCaching.class);
    }
}
