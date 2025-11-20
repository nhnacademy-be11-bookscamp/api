package store.bookscamp.api.book.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.controller.request.RerankerRequest;
import store.bookscamp.api.book.controller.response.RerankerResponse;
import store.bookscamp.api.book.entity.BookCaching;
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
    private final BookAnswerService bookAnswerService;
    private final BookCachingIndexService cachingIndexService;

    public Page<BookSortDto> searchBooks(BookSearchRequest request) {
        NativeQueryBuilder qb = new NativeQueryBuilder();
        if (request.role().equals("admin")) {
            if (request.keyword() == null || request.keyword().isEmpty()) {
                Category category = null;
                if (request.categoryId() != null) {
                    category = categoryRepository.getCategoryById(request.categoryId());
                }
                return noKeyWordSearch(qb, request, category);
            }// 키워드 없이 카테고리만 검색
            return adminSearchWithRRF(request);
        }

        if (request.keyword() != null && !request.keyword().equals("")) {
            Optional<BookCaching> cache = cachingIndexService.getCache(request.keyword());
            if (cache.isPresent()) {
                return convertToSearchResponse(cache.get(), request);
            }
        }
        if (request.keyword() == null || request.keyword().isEmpty()) {
            Category category = null;
            if (request.categoryId() != null) {
                category = categoryRepository.getCategoryById(request.categoryId());
            }
            return noKeyWordSearch(qb, request, category);
        }// 키워드 없이 카테고리만 검색
        return hybridSearchWithLLM(request);
    }

    //keyword 없을 시 전체검색 혹은 카테고리 분류
    public Page<BookSortDto> noKeyWordSearch(NativeQueryBuilder qb, BookSearchRequest request, Category category) {
        if (category != null && !category.getName().isBlank()) {
            qb.withFilter(f -> f.term(t -> t.field("category").value(category.getName())));
        }
        qb.withQuery(q -> q.matchAll(m -> m));

        qb.withPageable(PageRequest.of(0, 10000));
        Query query = qb.build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);

        List<BookDocument> documents = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        documents = applySortBookDocument(documents, request.sortType());

        Pageable pageable = request.pageable();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), documents.size());

        List<BookDocument> sliced = documents.subList(start, end);

        return new PageImpl<>(sliced.stream().map(BookSortDto::fromDocument).toList(), request.pageable(),
                documents.size());
    }


    // Gemini LLM 검증
    public Page<BookSortDto> hybridSearchWithLLM(BookSearchRequest request) {
        // 기존 hybridSearchWithRRF 결과 가져오기
        List<BookDocument> docs = hybridSearchWithRRF(request);
        List<BookDocument> topDocs = docs.stream().limit(10).toList();
        // Gemini 응답 호출
        Map<String, Object> aiResponse = null;
        aiResponse = bookAnswerService.generateAnswer(request.keyword(), topDocs);
        List<BookSortDto> dtoList;

        if (aiResponse.containsKey("result")) {
            // LLM 실패
            dtoList = docs.stream()
                    .map(BookSortDto::fromDocument)
                    .toList();
            for (int i = 0; i < dtoList.size(); i++) {
                dtoList.get(i).setAiRank(i + 1);
            }
        } else {
            // LLM 성공
            List<Long> idList = (List<Long>) aiResponse.get("idList");
            List<String> recList = (List<String>) aiResponse.get("recList");
            dtoList = buildDtoWithAiInfo(docs, idList, recList);
        }

        cachingIndexService.saveCache(request.keyword(), dtoList);
        List<BookSortDto> sorted =
                applySortAfterSortDto(dtoList, request.sortType());

        Pageable pageable = request.pageable();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());

        if (start >= sorted.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, sorted.size());
        }

        List<BookSortDto> pageSlice = sorted.subList(start, end);
        return new PageImpl<>(pageSlice, pageable, sorted.size());
    }


    // 통합 검색 (BM25 + Reranker + KNN + RRF)
    public List<BookDocument> hybridSearchWithRRF(BookSearchRequest request) {
        String keyword = request.keyword();
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.getCategoryById(request.categoryId());
        }

        // BM25 검색 (키워드)
        List<BookDocument> bm25Results = runBm25Search(category, keyword, 20);
        System.out.println("bm25Results size : " + bm25Results.size());
        for (BookDocument bm : bm25Results) {
            System.out.println("bm25Results: " + bm.getTitle());
        }

        // KNN 검색 (벡터)
        List<BookDocument> knnResults = runKnnSearch(category, keyword, 10);
        System.out.println("knnResults size : " + knnResults.size());
        for (BookDocument kn : knnResults) {
            System.out.println("knnResults: " + kn.getTitle());
        }

        // RRF 융합
        List<BookDocument> fused = rrfFusion(bm25Results, knnResults);
        System.out.println("fused results size : " + fused.size());

        // Reranker 적용
        List<BookDocument> reranked = rerankResults(keyword, fused);
        for (BookDocument rerank : reranked) {
            System.out.println("rerank : " + rerank.getTitle());
        }

        return reranked;
    }


    // BM25 (multiMatch)
    private List<BookDocument> runBm25Search(Category category, String keyword, int size) {
        NativeQueryBuilder qb = new NativeQueryBuilder();
        if (keyword != null && !keyword.isBlank()) {
            if (category != null) {
                qb.withFilter(f -> f.term(t -> t.field("category").value(category.getName())));
            }
            qb.withQuery(multiMatch(m -> m.query(keyword)
                    .fields("title^100", "contributors^90", "tags^80", "isbn^70", "publisher^60", "explanation^50",
                            "reviews^40")));
        } else {
            qb.withQuery(q -> q.matchAll(m -> m));
        }

        qb.withPageable(PageRequest.of(0, size));
        Query query = qb.build();
        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    // KNN (벡터 기반 의미 검색)
    private List<BookDocument> runKnnSearch(Category category, String keyword, int size) {
        String combinedText = "";
        if (category != null && category.getName() != null && !category.getName().isBlank()) {
            combinedText = keyword + " " + category.getName() + " " + category.getName();
        } else {
            combinedText = keyword;
        }
        log.info("knn-combined-text : " + combinedText);
        List<Float> vectorList = toFloatList(generateEmbedding(combinedText));

        NativeQuery query = NativeQuery.builder()
                .withKnnSearches(knn -> knn
                        .field("bookVector")
                        .queryVector(vectorList)
                        .k(size)
                        .numCandidates(Math.max(150, size))
                )
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }


    // Reranker API (문맥 재정렬)
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

    // RRF 융합 알고리즘
    private List<BookDocument> rrfFusion(List<BookDocument> listA, List<BookDocument> listB) {
        int k = 20; // 안정화 상수
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, BookDocument> allDocs = new HashMap<>();
        double weightA = 1.0;  // BM25
        double weightB = 4.0;  // KNN

        // (BM25+Reranker)
        for (int i = 0; i < listA.size(); i++) {
            BookDocument doc = listA.get(i);
            allDocs.putIfAbsent(doc.getId(), doc);
            scores.merge(doc.getId(), weightA * (1.0 / (k + i + 1)), Double::sum);
        }

        // (KNN)
        for (int i = 0; i < listB.size(); i++) {
            BookDocument doc = listB.get(i);
            allDocs.putIfAbsent(doc.getId(), doc);
            scores.merge(doc.getId(), weightB * (1.0 / (k + i + 1)), Double::sum);
        }

        // 점수 순 정렬
        return allDocs.values().stream()
                .sorted((d1, d2) -> Double.compare(scores.get(d2.getId()), scores.get(d1.getId())))
                .toList();
    }

    // Ollama 임베딩 호출
    private float[] generateEmbedding(String text) {
        try {
            String queryText = "Represent the meaning of the following user query: " + text;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ollama.java21.net/api/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                                { "model": "bge-m3", "prompt": "%s" }
                            """.formatted(queryText)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray embeddingArray = new JSONObject(response.body()).getJSONArray("embedding");

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

    //BookDoc단에서 정렬
    private List<BookDocument> applySortBookDocument(List<BookDocument> docs, String sortType) {
        return switch (sortType) {
            case "title" -> docs.stream()
                    .sorted(Comparator.comparing(BookDocument::getTitle,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
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

    //Ai정보를 기반으로 DTO 빌드
    private List<BookSortDto> buildDtoWithAiInfo(List<BookDocument> docs,
                                                 List<Long> idList,
                                                 List<String> recList) {
        Map<Long, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < idList.size(); i++) {
            rankMap.put(idList.get(i), i + 1);
        }

        List<BookSortDto> ranked = new ArrayList<>();
        List<BookSortDto> notRanked = new ArrayList<>();

        for (BookDocument doc : docs) {
            Long id = doc.getId();

            if (rankMap.containsKey(id)) {
                int rank = rankMap.get(id);
                BookSortDto dto = BookSortDto.fromDocument(doc);
                dto.setAiRank(rank);

                if (rank <= 3) {
                    dto.setAiRecommand(recList.get(rank - 1));
                }

                ranked.add(dto);

            } else {
                BookSortDto dto = BookSortDto.fromDocument(doc);
                notRanked.add(dto);
            }
        }

        ranked.sort(Comparator.comparingInt(BookSortDto::getAiRank));

        int currentRank = ranked.size() + 1;
        for (BookSortDto dto : notRanked) {
            dto.setAiRank(currentRank++);
        }

        ranked.addAll(notRanked);
        return ranked;
    }

    private Page<BookSortDto> convertToSearchResponse(BookCaching cache, BookSearchRequest request) {

        String sortType = request.sortType();
        Pageable pageable = request.pageable();

        List<BookSortDto> docs = new ArrayList<>(cache.getBooks());

        docs = applySortAfterSortDto(docs, sortType);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), docs.size());

        if (start >= docs.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, docs.size());
        }

        List<BookSortDto> pageSlice = docs.subList(start, end);

        return new PageImpl<>(pageSlice, pageable, docs.size());
    }

    private List<BookSortDto> applySortAfterSortDto(List<BookSortDto> dtos, String sortType) {

        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        dtos = switch (sortType) {

            case "title" -> dtos.stream()
                    .sorted(Comparator.comparing(BookSortDto::getTitle,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            case "bookLike" -> dtos.stream()
                    .sorted(Comparator.comparingLong(BookSortDto::getViewCount)
                            .reversed())
                    .toList();

            case "publishDate" -> dtos.stream()
                    .sorted(Comparator.comparing(BookSortDto::getPublishDate,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            case "low-price" -> dtos.stream()
                    .sorted(Comparator.comparingInt(BookSortDto::getSalePrice))
                    .toList();

            case "high-price" -> dtos.stream()
                    .sorted(Comparator.comparingInt(BookSortDto::getSalePrice)
                            .reversed())
                    .toList();

            case "rating" -> dtos.stream()
                    .sorted(Comparator.comparingDouble(BookSortDto::getAverageRating)
                            .reversed())
                    .toList();

            case "review" -> dtos.stream()
                    .sorted(Comparator.comparingLong(BookSortDto::getReviewCount)
                            .reversed())
                    .toList();
            default -> dtos.stream().sorted(Comparator.comparingInt(BookSortDto::getAiRank)).toList();
        };
        return dtos;

    }


    private Page<BookSortDto> adminSearchWithRRF(BookSearchRequest request) {
        List<BookDocument> docs = hybridSearchWithRRF(request);

        List<BookSortDto> dtoList = docs.stream()
                .map(BookSortDto::fromDocument)
                .toList();
        for (int i = 0; i < dtoList.size(); i++) {
            dtoList.get(i).setAiRank(i + 1);
        }
        List<BookSortDto> sorted =
                applySortAfterSortDto(dtoList, request.sortType());

        Pageable pageable = request.pageable();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());

        if (start >= sorted.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, sorted.size());
        }

        List<BookSortDto> pageSlice = sorted.subList(start, end);
        return new PageImpl<>(pageSlice, pageable, sorted.size());
    }

}


