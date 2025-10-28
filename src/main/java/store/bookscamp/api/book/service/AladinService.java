package store.bookscamp.api.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.book.service.dto.AladinResponse;

@Service
@RequiredArgsConstructor
public class AladinService {

    private final @Qualifier("aladinClient") WebClient aladinWebClient;


    @Value("${aladin.ttb-key}")
    private String ttbKey;
    @Value("${aladin.output}")
    private String output;
    @Value("${aladin.version}")
    private String version;


    /** 1) 리스트(베스트셀러/신간 등) - ItemList.aspx */
    public Mono<AladinResponse> fetchList(String queryType,
                                          Integer categoryId,
                                          Integer start,
                                          Integer maxResults) {

        return aladinWebClient.get()
                .uri(b -> b.path("/ItemList.aspx")
                        .queryParam("ttbkey", ttbKey)
                        .queryParam("QueryType", queryType)                 // e.g., Bestseller, ItemNewAll ...
                        .queryParamIfPresent("CategoryId", Optional.ofNullable(categoryId))
                        .queryParam("MaxResults", defaultInt(maxResults, 10))
                        .queryParam("start", defaultInt(start, 1))
                        .queryParam("SearchTarget", "Book")
                        .queryParam("output", output)
                        .queryParam("Version", version)
                        .build()
                )
                .retrieve()
                .bodyToMono(AladinResponse.class);
    }

    /** 2) ISBN 단건 조회 - ItemLookUp.aspx */
    public Mono<AladinResponse> lookupByIsbn13(String isbn13) {
        return aladinWebClient.get()
                .uri(b -> b.path("/ItemLookUp.aspx")
                        .queryParam("ttbkey", ttbKey)
                        .queryParam("itemIdType", "ISBN13")
                        .queryParam("ItemId", isbn13)
                        .queryParam("output", output)
                        .queryParam("Version", version)
                        .build()
                )
                .retrieve()
                .bodyToMono(AladinResponse.class);
    }

    /** 3) 키워드 검색 - ItemSearch.aspx */
    public Mono<AladinResponse> search(String query,
                                       String queryType,   // Title | Author | Publisher | Keyword ...
                                       Integer start,
                                       Integer maxResults,
                                       String sort) {       // Accuracy | PublishTime | SalesPoint ...
        return aladinWebClient.get()
                .uri(b -> b.path("/ItemSearch.aspx")
                        .queryParam("ttbkey", ttbKey)
                        .queryParam("Query", query)
                        .queryParam("QueryType", Optional.ofNullable(queryType).orElse("Keyword"))
                        .queryParam("start", start)
                        .queryParam("MaxResults", maxResults)
                        .queryParam("Sort", Optional.ofNullable(sort).orElse("Accuracy"))
                        .queryParam("output", output)
                        .queryParam("Version", version)
                        .build()
                )
                .retrieve()
                .bodyToMono(AladinResponse.class);
    }

    // ===== 유틸 & 매핑 (엔티티용) =====
    private int defaultInt(Integer v, int d) {
        return v == null ? d : v;
    }

    private String defaultStr(String v, String d) {
        return (v == null || v.isBlank()) ? d : v;
    }

    public Book toBookEntity(AladinItem i,
                             store.bookscamp.api.contributor.entity.Contributor contributor,
                             store.bookscamp.api.book.entity.BookStatus status,
                             boolean packable) {
        // Book 엔티티 필드 구조에 맞춘 매핑 (publishDate: yyyy-MM[-dd] 허용)
        LocalDate publishDate = parseDate(i.getPubDate());

        return new Book(
                i.getTitle(),
                i.getDescription(),   // explanation
                i.getToc(),           // content
                i.getPublisher(),
                publishDate,
                i.getIsbn13(),
                contributor,
                status,
                packable,
                i.getPriceStandard(),
                i.getPriceSales(),
                100,
                0L
        );
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            return LocalDate.now();
        }
        // pubDate 표기가 다양해서 느슨하게 처리
        try {
            if (s.length() == 10) {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            if (s.length() == 7) {
                return LocalDate.parse(s + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            if (s.length() == 8) {
                return LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE);
            }
        } catch (Exception ignored) {
        }
        return LocalDate.now();
    }
}
