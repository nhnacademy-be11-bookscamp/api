package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import store.bookscamp.api.book.entity.BookCaching;
import store.bookscamp.api.book.repository.BookCachingRepository;
import store.bookscamp.api.book.service.dto.BookSortDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCachingIndexServiceTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private BookCachingRepository bookCachingRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private BookCachingIndexService service;

    @Test
    @DisplayName("getCache - 캐시가 존재하고 TTL이 지나지 않았으면 반환한다")
    void getCache_Hit() {
        String keyword = "java";
        BookCaching cache = BookCaching.builder()
                .keyword(keyword)
                .cachedAt(System.currentTimeMillis())
                .books(List.of())
                .build();

        when(bookCachingRepository.findById(keyword)).thenReturn(Optional.of(cache));

        Optional<BookCaching> result = service.getCache(keyword);

        assertThat(result).isPresent();
        assertThat(result.get().getKeyword()).isEqualTo(keyword);
        verify(bookCachingRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("getCache - 캐시가 존재하지만 TTL(1시간)이 지났으면 삭제하고 빈 값 반환")
    void getCache_Expired() {
        String keyword = "java";
        long pastTime = System.currentTimeMillis() - (1000 * 60 * 60 * 2);

        BookCaching expiredCache = BookCaching.builder()
                .keyword(keyword)
                .cachedAt(pastTime)
                .books(List.of())
                .build();

        when(bookCachingRepository.findById(keyword)).thenReturn(Optional.of(expiredCache));

        Optional<BookCaching> result = service.getCache(keyword);

        assertThat(result).isEmpty();
        verify(bookCachingRepository).deleteById(keyword);
    }

    @Test
    @DisplayName("getCache - 캐시가 없으면 빈 값 반환")
    void getCache_Miss() {
        when(bookCachingRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<BookCaching> result = service.getCache("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveCache - 정상적으로 저장 메서드가 호출된다")
    void saveCache_Success() {
        String keyword = "spring";
        List<BookSortDto> dtos = List.of(BookSortDto.builder().id(1L).build());

        service.saveCache(keyword, dtos);

        verify(bookCachingRepository).save(any(BookCaching.class));
    }

    @Test
    @DisplayName("invalidateCachesContainingBook - 특정 책이 포함된 캐시를 검색하여 삭제한다")
    void invalidateCachesContainingBook_Success() {
        Long bookId = 100L;
        BookCaching cacheHit = BookCaching.builder().keyword("hit").build();

        SearchHit<BookCaching> hit = mock(SearchHit.class);
        when(hit.getId()).thenReturn("hit");

        SearchHits<BookCaching> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(hit));

        when(elasticsearchOperations.search(any(Query.class), eq(BookCaching.class)))
                .thenReturn(hits);

        service.invalidateCachesContainingBook(bookId);

        verify(elasticsearchOperations).search(any(Query.class), eq(BookCaching.class));
        verify(elasticsearchOperations).delete("hit", BookCaching.class);
    }
}