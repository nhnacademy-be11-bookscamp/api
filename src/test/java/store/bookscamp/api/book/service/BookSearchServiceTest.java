package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import store.bookscamp.api.book.controller.response.RerankerResponse;
import store.bookscamp.api.book.entity.BookCaching;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.feign.RerankerClient;
import store.bookscamp.api.book.service.dto.BookSearchRequest;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private ElasticsearchOperations esOps;
    @Mock
    private RerankerClient rerankerClient;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookAnswerService bookAnswerService;
    @Mock
    private BookCachingIndexService cachingIndexService;

    @InjectMocks
    private BookSearchService service;

    private SearchHits<BookDocument> mockSearchHits(List<BookDocument> docs) {
        List<SearchHit<BookDocument>> hitList = docs.stream().map(doc -> {
            SearchHit<BookDocument> hit = mock(SearchHit.class);
            lenient().when(hit.getContent()).thenReturn(doc);
            return hit;
        }).toList();

        SearchHits<BookDocument> hits = mock(SearchHits.class);
        lenient().when(hits.getSearchHits()).thenReturn(hitList);
        return hits;
    }

    private BookDocument createDoc(Long id, String title, int price, long viewCount, double rating) {
        return BookDocument.builder()
                .id(id)
                .title(title)
                .salePrice(price)
                .viewCount(viewCount)
                .averageRating(rating)
                .explanation("desc")
                .build();
    }

    @Test
    @DisplayName("searchBooks(Admin) - 키워드가 없으면 카테고리/전체 검색 (noKeyWordSearch)")
    void searchBooks_Admin_NoKeyword() {
        BookSearchRequest req = new BookSearchRequest(1L, null, "title", PageRequest.of(0, 10), "admin");

        Category category = mock(Category.class);
        lenient().when(category.getName()).thenReturn("IT");

        when(categoryRepository.getCategoryById(1L)).thenReturn(category);

        BookDocument doc = createDoc(1L, "Java", 1000, 0, 0.0);
        SearchHits<BookDocument> hits = mockSearchHits(List.of(doc));

        when(esOps.search(any(Query.class), eq(BookDocument.class))).thenReturn(hits);

        Page<BookSortDto> result = service.searchBooks(req);

        assertThat(result.getContent()).hasSize(1);
        verify(esOps).search(any(Query.class), eq(BookDocument.class));
    }

    @Test
    @DisplayName("searchBooks(Admin) - 키워드가 있으면 RRF 검색 수행 (adminSearchWithRRF)")
    void searchBooks_Admin_WithKeyword() {
        BookSearchRequest req = new BookSearchRequest(null, "java", "title", PageRequest.of(0, 10), "admin");

        SearchHits<BookDocument> bm25Hits = mockSearchHits(List.of(createDoc(1L, "Java Basic", 100, 0, 0)));

        when(esOps.search(any(Query.class), eq(BookDocument.class)))
                .thenReturn(bm25Hits);

        when(rerankerClient.rerank(any())).thenReturn(Collections.emptyList());

        Page<BookSortDto> result = service.searchBooks(req);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getAiRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("searchBooks(User) - 캐시가 존재하면 캐시된 데이터 반환")
    void searchBooks_User_CacheHit() {
        String keyword = "spring";
        BookSortDto cachedDto = BookSortDto.builder().id(1L).title("Cached Book").build();
        BookCaching cache = BookCaching.builder().keyword(keyword).books(List.of(cachedDto)).build();

        when(cachingIndexService.getCache(keyword)).thenReturn(Optional.of(cache));

        BookSearchRequest req = new BookSearchRequest(null, keyword, "title", PageRequest.of(0, 10), "user");

        Page<BookSortDto> result = service.searchBooks(req);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Cached Book");
        verify(esOps, never()).search(any(Query.class), eq(BookDocument.class));
    }

    @Test
    @DisplayName("noKeyWordSearch - 정렬 로직 검증 (가격 낮은순, 높은순, 평점순, 리뷰순 등)")
    void noKeyWordSearch_Sorting() {
        BookDocument d1 = createDoc(1L, "A_Book", 1000, 10, 4.5); // Low Price
        BookDocument d2 = createDoc(2L, "B_Book", 2000, 5, 5.0);  // High Rating
        BookDocument d3 = createDoc(3L, "C_Book", 3000, 100, 3.0); // High View

        SearchHits<BookDocument> hits = mockSearchHits(List.of(d1, d2, d3));
        lenient().when(esOps.search(any(Query.class), eq(BookDocument.class))).thenReturn(hits);

        NativeQueryBuilder qb = new NativeQueryBuilder();
        Category cat = null;

        BookSearchRequest reqLowPrice = new BookSearchRequest(null, null, "low-price", PageRequest.of(0, 10), "user");
        Page<BookSortDto> resLow = service.noKeyWordSearch(qb, reqLowPrice, cat);
        assertThat(resLow.getContent().get(0).getId()).isEqualTo(1L); // 1000원

        BookSearchRequest reqHighPrice = new BookSearchRequest(null, null, "high-price", PageRequest.of(0, 10), "user");
        Page<BookSortDto> resHigh = service.noKeyWordSearch(qb, reqHighPrice, cat);
        assertThat(resHigh.getContent().get(0).getId()).isEqualTo(3L); // 3000원

        BookSearchRequest reqRating = new BookSearchRequest(null, null, "rating", PageRequest.of(0, 10), "user");
        Page<BookSortDto> resRating = service.noKeyWordSearch(qb, reqRating, cat);
        assertThat(resRating.getContent().get(0).getId()).isEqualTo(2L); // 5.0점

        BookSearchRequest reqView = new BookSearchRequest(null, null, "bookLike", PageRequest.of(0, 10), "user");
        Page<BookSortDto> resView = service.noKeyWordSearch(qb, reqView, cat);
        assertThat(resView.getContent().get(0).getId()).isEqualTo(3L); // 100 view
    }

    @Test
    @DisplayName("hybridSearchWithLLM - LLM 성공 시 AI 추천 코멘트 및 순위 반영")
    void hybridSearchWithLLM_Success() {
        BookSearchRequest req = new BookSearchRequest(null, "java", "ai", PageRequest.of(0, 10), "user");

        BookDocument d1 = createDoc(10L, "Java 1", 100, 0, 0);
        BookDocument d2 = createDoc(20L, "Java 2", 100, 0, 0);

        SearchHits<BookDocument> bm25Hits = mockSearchHits(List.of(d1, d2));
        SearchHits<BookDocument> knnHits = mockSearchHits(List.of(d2, d1));

        when(esOps.search(any(Query.class), eq(BookDocument.class)))
                .thenReturn(bm25Hits) // BM25 result
                .thenReturn(knnHits); // KNN result

        when(rerankerClient.rerank(any())).thenReturn(Collections.emptyList());

        Map<String, Object> llmResult = new HashMap<>();
        llmResult.put("idList", List.of(20L, 10L));
        llmResult.put("recList", List.of("Strongly Recommend", "Recommend"));
        when(bookAnswerService.generateAnswer(anyString(), anyList())).thenReturn(llmResult);

        Page<BookSortDto> result = service.hybridSearchWithLLM(req);

        List<BookSortDto> content = result.getContent();

        assertThat(content.get(0).getId()).isEqualTo(20L);
        assertThat(content.get(0).getAiRank()).isEqualTo(1);
        assertThat(content.get(0).getAiRecommand()).isEqualTo("Strongly Recommend");

        assertThat(content.get(1).getId()).isEqualTo(10L);
        assertThat(content.get(1).getAiRank()).isEqualTo(2);

        verify(cachingIndexService).saveCache(eq("java"), anyList());
    }

    @Test
    @DisplayName("hybridSearchWithLLM - LLM 실패 시(result 키 존재) 기본 순서대로 순위 매김")
    void hybridSearchWithLLM_Fail() {
        BookSearchRequest req = new BookSearchRequest(null, "java", "ai", PageRequest.of(0, 10), "user");

        BookDocument d1 = createDoc(1L, "B1", 100, 0, 0);

        SearchHits<BookDocument> hits = mockSearchHits(List.of(d1));

        when(esOps.search(any(Query.class), eq(BookDocument.class)))
                .thenReturn(hits);

        when(rerankerClient.rerank(any())).thenReturn(Collections.emptyList());

        when(bookAnswerService.generateAnswer(anyString(), anyList()))
                .thenReturn(Map.of("result", "Fail"));

        Page<BookSortDto> result = service.hybridSearchWithLLM(req);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAiRank()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAiRecommand()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Reranker 로직 검증 - 재정렬 확인")
    void hybridSearch_RerankerLogic() {
        BookSearchRequest req = new BookSearchRequest(null, "java", "ai", PageRequest.of(0, 10), "admin");

        BookDocument docA = createDoc(1L, "A", 100, 0, 0);
        BookDocument docB = createDoc(2L, "B", 100, 0, 0);

        SearchHits<BookDocument> hits = mockSearchHits(List.of(docA, docB));

        when(esOps.search(any(Query.class), eq(BookDocument.class)))
                .thenReturn(hits);

        List<RerankerResponse> rerankResp = new ArrayList<>();
        rerankResp.add(new RerankerResponse(1, 0.9)); // docB
        rerankResp.add(new RerankerResponse(0, 0.1)); // docA

        when(rerankerClient.rerank(any())).thenReturn(rerankResp);

        Page<BookSortDto> result = service.searchBooks(req); // calls adminSearchWithRRF -> hybridSearchWithRRF

        assertThat(result.getContent().get(0).getId()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(1L);
    }
}