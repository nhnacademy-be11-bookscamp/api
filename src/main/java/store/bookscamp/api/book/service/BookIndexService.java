package store.bookscamp.api.book.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
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

    // DB Book → ES BookDocument 변환
    public BookDocument mapBookToDocument(Book book) {
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
                .packable(book.isPackable())
                .explanation(book.getExplanation())
                .averageRating(0.0)
                .reviewCount(0)
                .build();
    }

    public void indexBook(BookDocument book) {
        ElasticsearchOperations ops = elasticsearchOperations.withRefreshPolicy(RefreshPolicy.WAIT_UNTIL);

        ops.save(book);
        log.info("[BookIndexService] indexed book → {}", book.getTitle());
    }

    public void deleteBookIndex(Long bookId) {
        try {
            ElasticsearchOperations ops = elasticsearchOperations.withRefreshPolicy(RefreshPolicy.IMMEDIATE);
            ops.delete(String.valueOf(bookId), BookDocument.class);
            log.info("[BookIndexService] deleted book from index → id={}", bookId);
        } catch (Exception e) {
            log.error("[BookIndexService] delete failed → id={}", bookId, e);
        }
    }

    public BookDocument projectionToDoc(BookProjection row) {

        // LocalDate를 문자열로 변환
        String dateStr = (row.getPublishDate() != null)
                ? row.getPublishDate().toString()
                : null;

        BookDocument doc = BookDocument.builder()
                .id(row.getId())
                .title(row.getTitle())
                .explanation(row.getExplanation())
                .content(row.getContent())
                .publisher(row.getPublisher())
                .category(row.getCategory())
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

    public float[] generateEmbedding(String text) {
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


