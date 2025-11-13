package store.bookscamp.api.book.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.controller.request.RerankerRequest;
import store.bookscamp.api.book.controller.response.RerankerResponse;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.feign.RerankerClient;
import store.bookscamp.api.book.service.dto.BookSearchRequest;
import store.bookscamp.api.book.service.dto.BookSortDto;

import java.util.*;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.multiMatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RerankerClient rerankerClient;
    private final CategoryRepository categoryRepository;
    @Value("${elasticsearch.index.name}")
    private String INDEX_NAME;

    public Page<BookSortDto> searchBooks(BookSearchRequest request) {
        NativeQueryBuilder qb = new NativeQueryBuilder();
        if (request.keyword() == null || request.keyword().isEmpty()) {
            Category category = null;
            if (request.categoryId() != null) {
                category = categoryRepository.getCategoryById(request.categoryId());
            }
            return noKeyWordSearch(qb, request, category);
        }// 키워드 없이 카테고리만 검색
        return hybridSearchWithRRF(request);

    }

    public Page<BookSortDto> noKeyWordSearch(NativeQueryBuilder qb, BookSearchRequest request, Category category) {
        if (category != null && category.getName().isBlank()) {
            qb.withQuery(multiMatch(m -> m.query(category.getName())
                    .fields("category^100", "title^90", "contributors^80", "tags^70", "isbn^60", "publisher^50",
                            "explanation^40",
                            "reviews^30")));
        } else {
            qb.withQuery(q -> q.matchAll(m -> m));
        }

        qb.withPageable(request.pageable());
        Query query = qb.build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        List<BookDocument> documents = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        documents = applySortAfterRerank(documents, request.sortType());
        return new PageImpl<>(documents.stream().map(BookSortDto::fromDocument).toList(), request.pageable(),
                hits.getTotalHits());
    }

    // ================================================
    // ✅ 통합 검색 (BM25 + Reranker + KNN + RRF)
    // ================================================
    public Page<BookSortDto> hybridSearchWithRRF(BookSearchRequest request) {
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.getCategoryById(request.categoryId());
        }
        String keyword = request.keyword();
        Pageable pageable = request.pageable();

        // 1️⃣ BM25 검색 (키워드)
        List<BookDocument> bm25Results = runBm25Search(category, keyword, 100);

        // 2️⃣ KNN 검색 (벡터)
        List<BookDocument> knnResults = runKnnSearch(category,keyword, 100);

        // 3️⃣ RRF 융합
        List<BookDocument> fused = rrfFusion(bm25Results, knnResults);

        // 4️⃣ Reranker 적용
        List<BookDocument> reranked = rerankResults(keyword, fused);

        // 5️⃣ 상위 20개 반환
        List<BookDocument> topDocs = reranked.stream().limit(20).toList();

        topDocs = applySortAfterRerank(topDocs, request.sortType());
        return new PageImpl<>(topDocs.stream().map(BookSortDto::fromDocument).toList(),
                pageable, fused.size());
    }


    // ================================================
    // ✅ BM25 (multiMatch)
    // ================================================
    private List<BookDocument> runBm25Search(Category category, String keyword, int size) {
        NativeQueryBuilder qb = new NativeQueryBuilder();

        if (keyword != null && !keyword.isBlank()) {
            if(category!=null) {
                qb.withQuery(multiMatch(m -> m.query(keyword)
                        .fields("category^100","title^90", "contributors^80", "tags^70", "isbn^60", "publisher^50", "explanation^40",
                                "reviews^30")));
            }else{
                qb.withQuery(multiMatch(m -> m.query(keyword)
                        .fields("title^100", "contributors^90", "tags^80", "isbn^70", "publisher^60", "explanation^50",
                                "reviews^40")));
            }
        } else {
            qb.withQuery(q -> q.matchAll(m -> m));
        }

        qb.withPageable(PageRequest.of(0,size));
        Query query = qb.build();
        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    // ================================================
    // ✅ KNN (벡터 기반 의미 검색)
    // ================================================
    private List<BookDocument> runKnnSearch(Category category, String keyword,int size) {
        String combinedText = "";
        if (category != null && category.getName().isBlank()) {
            combinedText = String.join(" ",
                    keyword != null ? keyword : "",
                    category.getName() != null ? category.getName() : ""
            );
        }else{
            combinedText=keyword;
        }
        List<Float> vectorList = toFloatList(generateEmbedding(combinedText));

        NativeQuery query = NativeQuery.builder()
                .withKnnSearches(knn -> knn
                        .field("book_vector")
                        .queryVector(vectorList)  // ✅ 여기서 List<Float> 사용
                        .k(size)
                        .numCandidates(150)
                )
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }


    // ================================================
    // ✅ Reranker API (문맥 재정렬)
    // ================================================
    private List<BookDocument> rerankResults(String keyword, List<BookDocument> docs) {
        if (docs.isEmpty()) {
            return docs;
        }

        List<String> texts = docs.stream()
                .map(d -> d.getTitle() + " " + d.getExplanation())
                .toList();

        List<RerankerResponse> reranked = rerankerClient.rerank(new RerankerRequest(keyword, texts));
        reranked.sort(Comparator.comparingDouble(RerankerResponse::score).reversed());

        List<BookDocument> reordered = new ArrayList<>();
        Set<Integer> used = new HashSet<>();

        for (var r : reranked) {
            int idx = r.index();
            if (idx >= 0 && idx < docs.size() && used.add(idx)) {
                reordered.add(docs.get(idx));
            }
        }
        for (int i = 0; i < docs.size(); i++) {
            if (!used.contains(i)) {
                reordered.add(docs.get(i));
            }
        }
        return reordered;
    }

    // ================================================
    // ✅ RRF 융합 알고리즘
    // ================================================
    private List<BookDocument> rrfFusion(List<BookDocument> listA, List<BookDocument> listB) {
        int k = 60; // 안정화 상수
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, BookDocument> allDocs = new HashMap<>();

        // A리스트 (BM25+Reranker)
        for (int i = 0; i < listA.size(); i++) {
            BookDocument doc = listA.get(i);
            allDocs.putIfAbsent(doc.getId(), doc);
            scores.merge(doc.getId(), 1.0 / (k + i + 1), Double::sum);
        }

        // B리스트 (KNN)
        for (int i = 0; i < listB.size(); i++) {
            BookDocument doc = listB.get(i);
            allDocs.putIfAbsent(doc.getId(), doc);
            scores.merge(doc.getId(), 1.0 / (k + i + 1), Double::sum);
        }

        // 점수 순 정렬
        return allDocs.values().stream()
                .sorted((d1, d2) -> Double.compare(
                        scores.getOrDefault(d2.getId(), 0.0),
                        scores.getOrDefault(d1.getId(), 0.0)
                ))
                .toList();
    }

    // ================================================
    // ✅ Ollama 임베딩 호출
    // ================================================
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
            return new float[1024];
        }
    }


    private List<Float> toFloatList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float f : array) {
            list.add(f);
        }
        return list;
    }

    private List<BookDocument> applySortAfterRerank(List<BookDocument> docs, String sortType) {
        return switch (sortType) {
            case "bookLike" -> docs.stream()
                    .sorted(Comparator.comparingLong(BookDocument::getViewCount).reversed())
                    .toList();
            case "publishDate" -> docs.stream()
                    .sorted(Comparator.comparing(BookDocument::getPublishDate).reversed())
                    .toList();
            case "low-price" -> docs.stream()
                    .sorted(Comparator.comparingInt(BookDocument::getSalePrice))
                    .toList();
            case "high-price" -> docs.stream()
                    .sorted(Comparator.comparingInt(BookDocument::getSalePrice).reversed())
                    .toList();
            case "rating" -> docs.stream()
                    .sorted(Comparator.comparingDouble(BookDocument::getAverageRating).reversed())
                    .toList();
            case "review" -> docs.stream()
                    .sorted(Comparator.comparingLong(BookDocument::getReviewCount).reversed())
                    .toList();
            default -> docs; // 기본은 rerank 결과 그대로 사용
        };
    }
}


