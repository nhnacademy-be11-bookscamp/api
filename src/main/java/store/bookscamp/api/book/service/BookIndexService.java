package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import java.nio.charset.StandardCharsets;
import java.util.List;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.repository.BookRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    @Value("${elasticsearch.index.name}")
    private String INDEX_NAME;
    private static final String SETTINGS_PATH = "elasticsearch/books-settings.json";
    private final BookRepository bookRepository;

    /**
     * ✅ 애플리케이션 시작 시 인덱스 존재 여부 확인
     */
    @PostConstruct
    public void init() {
        try {
             BooleanResponse exists=esClient.indices().exists(e -> e.index(INDEX_NAME));

            if (exists.value()) {
                DeleteIndexResponse deleteResp = esClient.indices().delete(d -> d.index(INDEX_NAME));
                if (deleteResp.acknowledged()) {
                    log.info("[BookIndexService] index '{}' deleted", INDEX_NAME);
                }
                return; // ✅ 반드시 리턴*/
            }//이미 존재하는 인덱스 삭제
            
            try (Reader r = new InputStreamReader(
                    new ClassPathResource(SETTINGS_PATH).getInputStream(),
                    StandardCharsets.UTF_8)) {

                CreateIndexResponse resp = esClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .withJson(r)
                );

                if (resp.acknowledged()) {
                    log.info("[BookIndexService] index '{}' created", INDEX_NAME);
                } else {
                    log.warn("[BookIndexService] create index '{}' not acknowledged", INDEX_NAME);
                }
            }//인덱스 생성

            List<BookProjection> rows = bookRepository.findAllBooksWithRatingAndReview();
            if(rows.isEmpty()){
                log.warn("[BookIndexService] no books found in DB, skipping indexing.");
                return;
            }

            esClient.bulk(b -> {
                b.index(INDEX_NAME);
                for (BookProjection row : rows) {
                    // 1️⃣ Projection → Document 변환
                    BookDocument doc = projectionToDoc(row);
                    // 2️⃣ 임베딩 생성
                    String combinedText = String.join(" ",
                            doc.getTitle() != null ? doc.getTitle() : "",
                            doc.getExplanation() != null ? doc.getExplanation() : "",
                            doc.getCategory() != null ? doc.getCategory() : ""
                    );
                    float[] embedding = generateEmbedding(combinedText);
                    doc.setBookVector(embedding);

                    // 3️⃣ JSON 변환
                    Map<String, Object> jsonDoc = convertDocumentToMap(doc);
                    jsonDoc.put("book_vector", embedding); // ✅ 벡터 추가

                    // 4️⃣ 인덱스 연산 추가
                    b.operations(op -> op
                            .index(idx -> idx
                                    .index(INDEX_NAME)
                                    .id(String.valueOf(doc.getId()))
                                    .document(jsonDoc)
                            )
                    );
                }
                return b;
            });

            log.info("[BookIndexService] ✅ Indexed {} books into {}", rows.size(), INDEX_NAME);
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

    public BookDocument projectionToDoc(BookProjection row) {

        // ✅ LocalDate를 문자열로 변환
        String dateStr = (row.getPublishDate() != null)
                ? row.getPublishDate().toString()
                : null;

        BookDocument doc = BookDocument.builder()
                .id(row.getId())
                .title(row.getTitle())
                .explanation(row.getExplanation())
                .content(row.getContent())
                .publisher(row.getPublisher())
                .publishDate(row.getPublishDate())
                .isbn(row.getIsbn())
                .contributors(row.getContributors())
                .regularPrice(row.getRegularPrice())
                .salePrice(row.getSalePrice())
                .stock(row.getStock())
                .viewCount(row.getViewCount())
                .packable(Boolean.TRUE.equals(row.getPackable()))
                .status(row.getStatus())
                .averageRating(row.getAverageRating() != null ? row.getAverageRating() : 0.0)
                .reviewCount(row.getReviewCount() != null ? row.getReviewCount() : 0L)
                .build();
        return doc;
    }

    private Map<String, Object> convertDocumentToMap(BookDocument doc) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", doc.getId());
        map.put("title", doc.getTitle());
        map.put("explanation", doc.getExplanation());
        map.put("content", doc.getContent());
        map.put("publisher", doc.getPublisher());
        map.put("publishDate", doc.getPublishDate() != null ? doc.getPublishDate().toString() : null); // ✅ LocalDate → String
        map.put("isbn", doc.getIsbn());
        map.put("contributors", doc.getContributors());
        map.put("regularPrice", doc.getRegularPrice());
        map.put("salePrice", doc.getSalePrice());
        map.put("stock", doc.getStock());
        map.put("viewCount", doc.getViewCount());
        map.put("packable", doc.isPackable());
        map.put("status", doc.getStatus());
        map.put("averageRating", doc.getAverageRating());
        map.put("reviewCount", doc.getReviewCount());
        return map;
    }

    private float[] generateEmbedding(String text) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ollama.java21.net/api/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                    { "model": "bge-m3", "prompt": "%s" }
                """.formatted(text)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray embeddingArray = json.getJSONArray("embedding");

            float[] vector = new float[embeddingArray.length()];
            for (int i = 0; i < embeddingArray.length(); i++) {
                vector[i] = (float) embeddingArray.getDouble(i);
            }
            return vector;
        } catch (Exception e) {
            log.error("embedding generation failed", e);
            return new float[1024]; // fallback vector
        }
    }

}


