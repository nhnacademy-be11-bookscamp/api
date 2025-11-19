package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import store.bookscamp.api.book.entity.BookCaching;
import store.bookscamp.api.book.repository.BookCachingRepository;
import store.bookscamp.api.book.service.dto.BookSortDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCachingIndexService {

    private final ElasticsearchClient esClient;
    private final BookCachingRepository bookCachingRepository;
    private final long TTL_MILLIS = 60 * 60 * 1000;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("bookscamp-caching")
    private String CACHING_INDEX;

    private static final String SETTINGS_PATH = "elasticsearch/bookscamp-caching-settings.json";

    @PostConstruct
    public void init() {
        try {
            // 1) 존재 여부 체크
            BooleanResponse exists = esClient.indices().exists(e -> e.index(CACHING_INDEX));

            if (exists.value()) {
                DeleteIndexResponse del = esClient.indices().delete(d -> d.index(CACHING_INDEX));
                if (del.acknowledged()) {
                    log.info("[BookCachingIndexService] deleted '{}'", CACHING_INDEX);
                }
            }

            // 2) settings/mapping JSON 로딩
            try (Reader reader = new InputStreamReader(
                    new ClassPathResource(SETTINGS_PATH).getInputStream(),
                    StandardCharsets.UTF_8)) {

                CreateIndexResponse created = esClient.indices().create(c -> c
                        .index(CACHING_INDEX)
                        .withJson(reader)
                );

                if (created.acknowledged()) {
                    log.info("[BookCachingIndexService] created index '{}'", CACHING_INDEX);
                } else {
                    log.warn("[BookCachingIndexService] index '{}' was NOT acknowledged", CACHING_INDEX);
                }
            }

        } catch (Exception e) {
            log.error("[BookCachingIndexService] index init failed", e);
        }
    }//서버 부트 시 caching index 초기화



    public Optional<BookCaching> getCache(String keyword) {
        Optional<BookCaching> cache = bookCachingRepository.findById(keyword);
        if (cache.isEmpty()) return Optional.empty();
        long now = System.currentTimeMillis();
        if (now - cache.get().getCachedAt() > TTL_MILLIS) {
            bookCachingRepository.deleteById(keyword);
            return Optional.empty();
        }
        return cache;
    }// 캐시값으로 삭제

    public void saveCache(
            String keyword,
            List<BookSortDto> books
    ) {
        BookCaching cache = BookCaching.builder()
                .keyword(keyword)
                .books(books)
                .cachedAt(System.currentTimeMillis())
                .build();

        bookCachingRepository.save(cache);
        log.info("[Cache Saved] keyword = {}", keyword);
    }

    public void invalidateCachesContainingBook(Long bookId) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.nested(n -> n
                        .path("books")
                        .query(nq -> nq.term(t -> t.field("books.id").value(bookId)))))
                .withPageable(PageRequest.of(0, 1000))
                .build();

        SearchHits<BookCaching> hits = elasticsearchOperations.search(query, BookCaching.class);

        for (SearchHit<BookCaching> hit : hits.getSearchHits()) {
            elasticsearchOperations.delete(hit.getId(), BookCaching.class);
        }
    }

}
