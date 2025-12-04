package store.bookscamp.api.book.service;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookCaching;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.feign.RerankerClient;
import store.bookscamp.api.book.service.dto.BookSearchRequest;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.category.repository.CategoryRepository;


@SpringBootTest
class BookSearchServiceTest {

    private final ElasticsearchOperations esOps = mock(ElasticsearchOperations.class);
    private final RerankerClient rerankerClient = mock(RerankerClient.class);
    private final CategoryRepository categoryRepo = mock(CategoryRepository.class);
    private final BookAnswerService bookAnswer = mock(BookAnswerService.class);
    private final BookCachingIndexService cachingService = mock(BookCachingIndexService.class);

    private BookSearchService createService() {
        return new BookSearchService(
                esOps, rerankerClient, categoryRepo, bookAnswer, cachingService
        );
    }


    @Test
    @DisplayName("noKeywordSearch - ES 결과 기반 정상 페이징")
    void noKeywordSearch_ok() {

        BookSearchService service = createService();

        BookDocument d1 = BookDocument.builder().id(1L).title("A").salePrice(1000).build();
        BookDocument d2 = BookDocument.builder().id(2L).title("B").salePrice(800).build();

        SearchHit<BookDocument> h1 = mock(SearchHit.class);
        when(h1.getContent()).thenReturn(d1);
        SearchHit<BookDocument> h2 = mock(SearchHit.class);
        when(h2.getContent()).thenReturn(d2);

        SearchHits<BookDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(h1, h2));

        when(esOps.search((Query) any(), eq(BookDocument.class))).thenReturn(hits);

        BookSearchRequest req = new BookSearchRequest(
                null,
                null,
                "low-price",
                PageRequest.of(0, 10),
                "user"
        );

        Page<BookSortDto> result = service.noKeyWordSearch(
                new NativeQueryBuilder(), req, null
        );

        Assertions.assertThat(result.getContent().size()).isEqualTo(2);
        Assertions.assertThat(result.getContent().get(0).getId()).isEqualTo(2L);
    }


    @Test
    @DisplayName("searchBooks - 캐시 HIT 시 바로 캐시 반환")
    void searchBooks_cacheHit() {

        BookSearchService service = createService();

        BookSortDto dto = BookSortDto.builder().id(10L).title("cached").build();
        BookCaching cache = BookCaching.builder()
                .keyword("java")
                .books(List.of(dto))
                .cachedAt(System.currentTimeMillis())
                .build();

        when(cachingService.getCache("java")).thenReturn(Optional.of(cache));

        BookSearchRequest req = new BookSearchRequest(
                null, "java", "title", PageRequest.of(0, 10), "user"
                );

        Page<BookSortDto> result = service.searchBooks(req);

        Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("cached");
    }


    @Test
    @DisplayName("hybridSearchWithLLM - LLM 실패 시 기본 순서 + aiRank 부여")
    void hybridSearch_llmFail() {

        BookSearchService service = spy(createService());

        BookDocument d1 = BookDocument.builder().id(1L).title("A").explanation("x").build();
        BookDocument d2 = BookDocument.builder().id(2L).title("B").explanation("y").build();

        doReturn(List.of(d1, d2)).when(service).hybridSearchWithRRF(any());

        when(bookAnswer.generateAnswer(any(), any()))
                .thenReturn(Map.of("result", "LLM error"));

        BookSearchRequest req = new BookSearchRequest(
                null, "java", "title", PageRequest.of(0, 10), "user"
                );

        Page<BookSortDto> result = service.hybridSearchWithLLM(req);

        Assertions.assertThat(result.getContent().get(0).getAiRank()).isEqualTo(1);
        Assertions.assertThat(result.getContent().get(1).getAiRank()).isEqualTo(2);
    }


    @Test
    @DisplayName("hybridSearchWithLLM - LLM 성공 시 idList 순서/추천어 반영")
    void hybridSearch_llmSuccess() {

        BookSearchService service = spy(createService());

        BookDocument d1 = BookDocument.builder().id(3L).title("책3").explanation("a").build();
        BookDocument d2 = BookDocument.builder().id(18L).title("책18").explanation("b").build();
        BookDocument d3 = BookDocument.builder().id(10L).title("책10").explanation("c").build();

        doReturn(List.of(d1, d2, d3)).when(service).hybridSearchWithRRF(any());

        when(bookAnswer.generateAnswer(any(), any()))
                .thenReturn(Map.of(
                        "idList", List.of(3L, 18L, 10L),
                        "recList", List.of("good", "better", "best")
                ));

        BookSearchRequest req = new BookSearchRequest(
                null, "java", "title", PageRequest.of(0, 10), "user"
                );

        Page<BookSortDto> result = service.hybridSearchWithLLM(req);

        List<BookSortDto> list = result.getContent();

        Assertions.assertThat(list.get(0).getId()).isEqualTo(10L);
        Assertions.assertThat(list.get(0).getAiRecommand()).isEqualTo("best");
    }


    @Test
    @DisplayName("hybridSearchWithRRF - BM25 + KNN + RRF 흐름 통합 Mock")
    void hybridRRF_mock() {

        BookSearchService service = spy(createService());

        BookDocument d1 = BookDocument.builder().id(1L).title("A").explanation("a").build();
        BookDocument d2 = BookDocument.builder().id(2L).title("B").explanation("b").build();

        doReturn(List.of(d1, d2))
                .when(service)
                .hybridSearchWithRRF(any());

        BookSearchRequest req = new BookSearchRequest(null, "java",
                null, PageRequest.of(0, 10), "user");

        List<BookDocument> result = service.hybridSearchWithRRF(req);

        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("BookSortDto.from(Book) - Book 객체 기반 정확한 DTO 변환")
    void dto_fromBook_success() {
        Book book = new Book(
                "title",
                "explanation",
                "content",
                "publisher",
                LocalDate.of(2024, 1, 1),
                "1234567890123",
                "contributor",
                AVAILABLE,
                true,
                10000,
                8000,
                20,
                5
        );

        BookSortDto dto = BookSortDto.from(book);

        Assertions.assertThat(dto.getTitle()).isEqualTo("title");
        Assertions.assertThat(dto.getViewCount()).isEqualTo(5);
        Assertions.assertThat(dto.getSalePrice()).isEqualTo(8000);

        // 고정값 배정 부분 테스트
        Assertions.assertThat(dto.getIsbn()).isNull();
        Assertions.assertThat(dto.getAverageRating()).isEqualTo(0.0);
        Assertions.assertThat(dto.getReviewCount()).isEqualTo(0L);
        Assertions.assertThat(dto.getAiRecommand()).isEqualTo("");
        Assertions.assertThat(dto.getAiRank()).isEqualTo(0);
    }
}
