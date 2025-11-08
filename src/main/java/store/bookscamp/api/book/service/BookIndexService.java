package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import java.io.InputStreamReader;
import java.io.Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "bookscamp";
    private static final String SETTINGS_PATH = "elasticsearch/books-settings.json";
    /**
     * ✅ 애플리케이션 시작 시 인덱스 존재 여부 확인
     */
    @PostConstruct
    public void init() {
        try {
             BooleanResponse exists=esClient.indices().exists(e -> e.index(INDEX_NAME));

            if (exists.value()) {
                log.info("[BookIndexService] index '{}' already exists ✅", INDEX_NAME);
                return; // ✅ 반드시 리턴
            }
            try (Reader r = new InputStreamReader(
                    new ClassPathResource(SETTINGS_PATH).getInputStream(),
                    StandardCharsets.UTF_8)) {

                CreateIndexResponse resp = esClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .withJson(r)
                );

                if (resp.acknowledged()) {
                    log.info("[BookIndexService] index '{}' created ✅", INDEX_NAME);
                } else {
                    log.warn("[BookIndexService] create index '{}' not acknowledged", INDEX_NAME);
                }
            }
        } catch (Exception e) {
            log.error("[BookIndexService] index init failed", e);
        }
    }

    /**
     * ✅ DB Book → ES BookDocument 변환
     */
    private BookDocument mapBookToDocument(Book book) {
        return BookDocument.builder()
                .id(book.getId())
                .title(book.getTitle())
                .contributors(book.getContributors())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .publishDate(book.getPublishDate())
                .regularPrice(book.getRegularPrice())
                .salePrice(book.getSalePrice())
                .stock(book.getStock())
                .viewCount(book.getViewCount())
                .status(book.getStatus().name())
                .explanation(book.getExplanation())
                .averageRating(0.0)
                .reviewCount(0)
                .build();
    }

    /**
     * ✅ 단일 도서 인덱싱
     */
    public void indexBook(Book book) {
        BookDocument doc = mapBookToDocument(book);
        elasticsearchOperations.save(doc);
        log.info("[BookIndexService] indexed book → {}", book.getTitle());
    }

    /**
     * ✅ 전체 도서 인덱싱
     */
    public void indexAllBooks(List<Book> books) {
        List<BookDocument> docs = books.stream()
                .map(this::mapBookToDocument)
                .toList();
        elasticsearchOperations.save(docs);
        log.info("[BookIndexService] indexed {} books ✅", docs.size());
    }
}


