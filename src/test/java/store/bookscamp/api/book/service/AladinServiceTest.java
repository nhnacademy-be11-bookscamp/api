package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.book.service.dto.AladinResponse;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AladinServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AladinService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "ttbKey", "TEST_KEY");
        ReflectionTestUtils.setField(service, "output", "json");
        ReflectionTestUtils.setField(service, "version", "20131101");
    }

    private void setupWebClient(AladinResponse response) {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);

        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenAnswer(invocation -> {
            Function<UriBuilder, URI> func = invocation.getArgument(0);
            UriBuilder uriBuilder = mock(UriBuilder.class);

            lenient().when(uriBuilder.path(anyString())).thenReturn(uriBuilder);

            lenient().when(uriBuilder.queryParam(anyString(), any(Object[].class))).thenReturn(uriBuilder);

            lenient().when(uriBuilder.queryParam(anyString(), any(Collection.class))).thenReturn(uriBuilder);

            lenient().when(uriBuilder.queryParamIfPresent(anyString(), any())).thenReturn(uriBuilder);

            lenient().when(uriBuilder.build()).thenReturn(URI.create("http://localhost"));

            func.apply(uriBuilder);

            return requestHeadersSpec;
        });

        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(AladinResponse.class)).thenReturn(Mono.just(response));
    }

    @Test
    void fetchList_fullParameters() {
        AladinResponse mockResponse = new AladinResponse();
        setupWebClient(mockResponse);

        Mono<AladinResponse> resultMono = service.fetchList("Bestseller", 100, 1, 10);
        AladinResponse result = resultMono.block();

        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void fetchList_withNulls() {
        AladinResponse mockResponse = new AladinResponse();
        setupWebClient(mockResponse);

        Mono<AladinResponse> resultMono = service.fetchList("Bestseller", null, null, null);
        AladinResponse result = resultMono.block();

        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void lookupByIsbn13_success() {
        AladinResponse mockResponse = new AladinResponse();
        setupWebClient(mockResponse);

        AladinResponse result = service.lookupByIsbn13("9781234567890").block();

        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void search_fullParameters() {
        AladinResponse mockResponse = new AladinResponse();
        setupWebClient(mockResponse);

        AladinResponse result = service.search("Java", "Keyword", 1, 10, "Accuracy").block();

        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void search_withNulls() {
        AladinResponse mockResponse = new AladinResponse();
        setupWebClient(mockResponse);

        AladinResponse result = service.search("Java", null, null, null, null).block();

        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void toBookEntity_success() {
        AladinItem item = new AladinItem();
        item.setTitle("Title");
        item.setDescription("Desc");
        item.setToc("Toc");
        item.setPublisher("Pub");
        item.setIsbn13("9781111222233");
        item.setPubDate("2024-01-10");
        item.setPriceStandard(20000);
        item.setPriceSales(18000);

        Book book = service.toBookEntity(item, "Author", BookStatus.AVAILABLE, true);

        assertThat(book.getTitle()).isEqualTo("Title");
        assertThat(book.getExplanation()).isEqualTo("Desc");
        assertThat(book.getContent()).isEqualTo("Toc");
        assertThat(book.getPublisher()).isEqualTo("Pub");
        assertThat(book.getIsbn()).isEqualTo("9781111222233");
        assertThat(book.getRegularPrice()).isEqualTo(20000);
        assertThat(book.getSalePrice()).isEqualTo(18000);
        assertThat(book.getPublishDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(book.isPackable()).isTrue();
        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    private LocalDate invokeParse(String s) throws Throwable {
        try {
            Method m = AladinService.class.getDeclaredMethod("parseDate", String.class);
            m.setAccessible(true);
            return (LocalDate) m.invoke(service, s);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void parseDate_validFormats() throws Throwable {
        assertThat(invokeParse("2024-01-05")).isEqualTo(LocalDate.of(2024, 1, 5));
        assertThat(invokeParse("2024-01")).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(invokeParse("20240105")).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    void parseDate_nullOrEmpty() throws Throwable {
        assertThat(invokeParse(null)).isEqualTo(LocalDate.now());
        assertThat(invokeParse("")).isEqualTo(LocalDate.now());
        assertThat(invokeParse("   ")).isEqualTo(LocalDate.now());
    }

    @Test
    void parseDate_invalidFormat_throwsException() {
        assertThatThrownBy(() -> invokeParse("2024-99-99"))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARSE_ERROR);
    }

    @Test
    void parseDate_lengthMismatch_returnsNow() throws Throwable {
        assertThat(invokeParse("invalid-length-string")).isEqualTo(LocalDate.now());
    }
}