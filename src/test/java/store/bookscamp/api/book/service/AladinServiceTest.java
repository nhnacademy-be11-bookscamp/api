package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.LocalDate;

import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.book.service.dto.AladinResponse;


class AladinServiceTest {

    private WebClient mockWebClient = mock(WebClient.class);
    private WebClient.RequestHeadersUriSpec uriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    private WebClient.RequestHeadersSpec headersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
    private WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);


    private AladinService createService() {

        when(mockWebClient.get()).thenReturn(uriSpec);

        when(uriSpec.uri((URI) any())).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(AladinResponse.class))
                .thenReturn(Mono.just(new AladinResponse()));

        AladinService service = new AladinService(mockWebClient);

        // 환경 변수 값 강제 주입
        setField(service, "ttbKey", "TEST_KEY");
        setField(service, "output", "json");
        setField(service, "version", "1");

        return service;
    }

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("fetchList - WebClient 호출 정상 작동")
    void fetchList_success() {
        AladinService service = createService();

        Mono<AladinResponse> mono = service.fetchList("Bestseller", 100, 1, 10);
        AladinResponse res = mono.block();

        assertThat(res).isNotNull();
        verify(mockWebClient, times(1)).get();
        verify(uriSpec, times(1)).uri(any(Function.class));
        verify(headersSpec, times(1)).retrieve();
    }

    @Test
    @DisplayName("lookupByIsbn13 - ISBN Query 정상 호출")
    void lookupByIsbn13_success() {
        // arrange
        AladinService service = createService();

        AladinResponse response = new AladinResponse();
        when(mockWebClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AladinResponse.class))
                .thenReturn(Mono.just(response));

        // act
        AladinResponse res = service.lookupByIsbn13("9781234567890").block();

        // assert
        assertThat(res).isNotNull();
        verify(mockWebClient, times(1)).get();
        verify(uriSpec, times(1)).uri(any(Function.class));
        verify(headersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToMono(AladinResponse.class);
    }


    @Test
    @DisplayName("search - QueryType/Sort 기본값 및 호출 확인")
    void search_success() {
        AladinService service = createService();

        AladinResponse res = service.search("자바", null, 1, 10, null).block();

        assertThat(res).isNotNull();
        verify(mockWebClient, times(1)).get();
        verify(uriSpec, times(1)).uri(any(Function.class));
    }

    @Test
    @DisplayName("defaultInt - null이면 default 반환")
    void defaultInt_null() throws Exception {
        AladinService service = createService();

        var m = AladinService.class.getDeclaredMethod("defaultInt", Integer.class, int.class);
        m.setAccessible(true);

        int result = (int) m.invoke(service, null, 5);
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("defaultInt - 값 있으면 그대로")
    void defaultInt_value() throws Exception {
        AladinService service = createService();

        var m = AladinService.class.getDeclaredMethod("defaultInt", Integer.class, int.class);
        m.setAccessible(true);

        int result = (int) m.invoke(service, 10, 5);
        assertThat(result).isEqualTo(10);
    }

    private LocalDate invokeParse(AladinService service, String s) throws Exception {
        var m = AladinService.class.getDeclaredMethod("parseDate", String.class);
        m.setAccessible(true);
        return (LocalDate) m.invoke(service, s);
    }

    @Test
    @DisplayName("parseDate - yyyy-MM-dd 정상 파싱")
    void parseDate_full() throws Exception {
        AladinService service = createService();
        LocalDate date = invokeParse(service, "2024-01-05");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("parseDate - yyyy-MM → 자동 1일 처리")
    void parseDate_yearMonth() throws Exception {
        AladinService service = createService();
        LocalDate date = invokeParse(service, "2024-01");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("parseDate - yyyyMMdd BASIC")
    void parseDate_basic() throws Exception {
        AladinService service = createService();
        LocalDate date = invokeParse(service, "20240105");
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("parseDate - null/빈값 → 오늘 날짜")
    void parseDate_null() throws Exception {
        AladinService service = createService();
        assertThat(invokeParse(service, null)).isEqualTo(LocalDate.now());
        assertThat(invokeParse(service, "")).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("parseDate - 잘못된 형식 → 오늘 날짜")
    void parseDate_error() throws Exception {
        AladinService service = createService();
        assertThat(invokeParse(service, "???")).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("toBookEntity - 매핑 정상 확인")
    void toBookEntity_success() {
        AladinService service = createService();

        AladinItem item = new AladinItem();
        item.setTitle("타이틀");
        item.setDescription("설명");
        item.setToc("목차");
        item.setPublisher("출판사");
        item.setIsbn13("9781111222233");
        item.setPubDate("2024-01-10");
        item.setPriceStandard(20000);
        item.setPriceSales(18000);

        Book book = service.toBookEntity(item, "저자A", BookStatus.AVAILABLE, true);

        assertThat(book.getTitle()).isEqualTo("타이틀");
        assertThat(book.getExplanation()).isEqualTo("설명");
        assertThat(book.getContent()).isEqualTo("목차");
        assertThat(book.getPublisher()).isEqualTo("출판사");
        assertThat(book.getIsbn()).isEqualTo("9781111222233");
        assertThat(book.getPublishDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(book.getRegularPrice()).isEqualTo(20000);
        assertThat(book.getSalePrice()).isEqualTo(18000);
        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(book.isPackable()).isTrue();
    }
}
