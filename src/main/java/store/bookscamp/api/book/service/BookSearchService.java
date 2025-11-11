package store.bookscamp.api.book.service;

import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.multiMatch;

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RerankerClient rerankerClient;

    public Page<BookSortDto> searchBooks(BookSearchRequest request) {

        NativeQueryBuilder qb = new NativeQueryBuilder();

        if (request.keyword() != null && !request.keyword().isBlank()) {
            qb.withQuery(multiMatch(m -> m.query(request.keyword())
                    .fields("title^100", "contributors^90", "tags^80", "isbn^70", "publisher^60", "explanation^50",
                            "reviews^40")));
        } else {
            qb.withQuery(q -> q.matchAll(m -> m));
        }

        applySort(qb, request.sortType());

        qb.withPageable(request.pageable());
        Query query = qb.build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(query, BookDocument.class);
        List<BookDocument> documents = hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        if (request.keyword() != null && !request.keyword().isBlank() && !documents.isEmpty()) {
            List<String> texts = documents.stream().map(doc -> doc.getTitle() + " " + doc.getExplanation()).toList();
            List<RerankerResponse> reranked = rerankerClient.rerank(
                    new RerankerRequest(request.keyword(), texts));

            // 점수 내림차순
            reranked.sort(Comparator.comparingDouble(RerankerResponse::score).reversed());

            List<BookDocument> reordered = new ArrayList<>(documents.size());
            Set<Integer> used = new HashSet<>();

            // 1) 리랭커가 준 순서대로 안전히 담기
            for (var r : reranked) {
                int idx = r.index();
                if (idx >= 0 && idx < documents.size() && used.add(idx)) {
                    reordered.add(documents.get(idx));
                }
            }

            // 2) 누락된 문서들 원래 순서대로 뒤에 채우기
            for (int i = 0; i < documents.size(); i++) {
                if (!used.contains(i)) {
                    reordered.add(documents.get(i));
                }
            }

            // 최종 치환
            documents = reordered;

        }

        return new PageImpl<>(documents.stream().map(BookSortDto::fromDocument).toList(), request.pageable(),
                hits.getTotalHits());
    }

    private void applySort(NativeQueryBuilder qb, String sortType) {
        switch (sortType) {
            case "bookLike" -> qb.withSort(Sort.by(Sort.Order.desc("viewCount")));
            case "publishDate" -> qb.withSort(Sort.by(Sort.Order.desc("publishDate")));
            case "low-price" -> qb.withSort(Sort.by(Sort.Order.asc("salePrice")));
            case "high-price" -> qb.withSort(Sort.by(Sort.Order.desc("salePrice")));
            case "rating" -> {
                qb.withQuery(q -> q.range(r -> r.field("reviewCount").gte(JsonData.of(100))));
                qb.withSort(Sort.by(Sort.Order.desc("averageRating")));
            }
            case "review" -> qb.withSort(Sort.by(Sort.Order.desc("reviewCount")));
            default -> qb.withSort(Sort.by(Sort.Order.desc("_score")));
        }
    }
}


