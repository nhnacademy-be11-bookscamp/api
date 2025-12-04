package store.bookscamp.api.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomConfigClientWatchTest {

    @Mock private ConfigDataContextRefresher refresher;
    @Mock private ConfigServicePropertySourceLocator locator;
    @Mock private Environment environment;
    @InjectMocks private CustomConfigClientWatch watch;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(watch, "enabled", true);
        watch.setEnvironment(environment);
    }

    @Test
    @DisplayName("1. [정상] Config 버전이 변경되면(null -> v1) refresh가 호출되어야 한다")
    void refresh_when_version_changed_initial() {
        setupEnvironmentProps();
        watch.start();

        mockLocatorReturnVersion("v1");

        watch.watchConfigServer();

        verify(refresher, times(1)).refresh();
    }

    @Test
    @DisplayName("2. [정상] Config 버전이 변경되면(v1 -> v2) refresh가 호출되어야 한다")
    void refresh_when_version_changed_update() {
        setupEnvironmentProps();
        watch.start();

        AtomicReference<String> versionRef = (AtomicReference<String>) ReflectionTestUtils.getField(watch, "version");
        versionRef.set("v1");

        mockLocatorReturnVersion("v2");

        watch.watchConfigServer();

        verify(refresher, times(1)).refresh();
    }

    @Test
    @DisplayName("3. [스킵] 버전이 동일하면(v1 -> v1) refresh 하지 않는다")
    void skip_when_version_same() {
        setupEnvironmentProps();
        watch.start();

        AtomicReference<String> versionRef = (AtomicReference<String>) ReflectionTestUtils.getField(watch, "version");
        versionRef.set("v1");

        mockLocatorReturnVersion("v1");

        watch.watchConfigServer();

        verify(refresher, never()).refresh();
    }

    @Test
    @DisplayName("4. [비활성] enabled=false 설정이면 start()가 동작하지 않아 watch도 실행되지 않는다")
    void do_nothing_when_disabled() {
        ReflectionTestUtils.setField(watch, "enabled", false);

        watch.start();

        watch.watchConfigServer();

        verify(locator, never()).locate(any());
    }

    @Test
    @DisplayName("5. [상태] start()를 호출하지 않아 running=false면 실행되지 않는다")
    void do_nothing_when_not_running() {

        watch.watchConfigServer();

        verify(locator, never()).locate(any());
    }

    @Test
    @DisplayName("6. [예외] Config Server 통신 중 예외가 발생하면 안전하게 무시한다 (로그 출력)")
    void safe_ignore_when_exception_occurs() {
        setupEnvironmentProps();
        watch.start();

        given(locator.locate(any())).willThrow(new RuntimeException("Connection Refused"));

        watch.watchConfigServer();

        verify(refresher, never()).refresh();
    }

    @Test
    @DisplayName("7. [Null] 가져온 버전이 null이면(키 없음) 무시한다")
    void ignore_when_fetched_version_is_null() {
        setupEnvironmentProps();
        watch.start();

        CompositePropertySource compositeSource = new CompositePropertySource("config");
        MapPropertySource mapSource = new MapPropertySource("source", Collections.emptyMap());
        compositeSource.addPropertySource(mapSource);

        given(locator.locate(any())).willReturn((PropertySource) compositeSource);

        watch.watchConfigServer();

        verify(refresher, never()).refresh();
    }

    @Test
    @DisplayName("8. [종료] close() 호출 시 running=false가 되어 이후 watch가 동작하지 않는다")
    void stop_watching_when_closed() {
        setupEnvironmentProps();
        watch.start();
        watch.close();

        watch.watchConfigServer();

        verify(locator, never()).locate(any());
    }


    private void setupEnvironmentProps() {
        given(environment.getProperty("config.watch.initialDelay", "10000")).willReturn("1000");
        given(environment.getProperty("config.watch.delay", "20000")).willReturn("2000");
    }

    private void mockLocatorReturnVersion(String version) {
        CompositePropertySource compositeSource = new CompositePropertySource("config");
        MapPropertySource mapSource = new MapPropertySource("source", Map.of("config.client.version", version));
        compositeSource.addPropertySource(mapSource);

        given(locator.locate(any())).willReturn((PropertySource) compositeSource);
    }
}