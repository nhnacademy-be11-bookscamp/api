package store.bookscamp.api.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.context.EnvironmentAware;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class CustomConfigClientWatch implements Closeable, EnvironmentAware {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<String> version = new AtomicReference<>();

    private final ConfigDataContextRefresher refresher;
    private final ConfigServicePropertySourceLocator locator;

    private Environment environment;

    @Value("${config.watch.enabled:true}")
    private boolean enabled;

    public CustomConfigClientWatch(ConfigDataContextRefresher refresher,
                                   ConfigServicePropertySourceLocator locator) {
        this.refresher = refresher;
        this.locator = locator;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("[Config Watch] disabled by config.watch.enabled=false");
            return;
        }
        running.compareAndSet(false, true);
        log.info("[Config Watch] started (initialDelay={}ms, delay={}ms)",
                environment.getProperty("config.watch.initialDelay", "10000"),
                environment.getProperty("config.watch.delay", "20000"));

    }

    @Scheduled(
            initialDelayString = "${config.watch.initialDelay:10000}",
            fixedDelayString = "${config.watch.delay:20000}"
    )
    public void watchConfigServer() {
        if (!running.get()) {
            return;
        }

        String newVersion = fetchNewVersion();
        if (newVersion == null) {
            return;
        }

        String oldVersion = version.get();
        if (versionChanged(oldVersion, newVersion)) {
            log.info("[Config Watch] version changed: {} -> {}", oldVersion, newVersion);

            version.set(newVersion);
            // refresh() = /actuator/refresh 와 동일한 동작
            refresher.refresh();

            String initialDelay = environment.getProperty("config.watch.initialDelay", "10000");
            String fixedDelay = environment.getProperty("config.watch.delay", "20000");

            log.info("[Config Watch] Context refreshed successfully!");
            log.info("[Config Watch] new scheduled values: initialDelay={}ms, delay={}ms",
                    initialDelay,
                    fixedDelay
            );
        }
    }

    private String fetchNewVersion() {
        try {
            CompositePropertySource propertySource =
                    (CompositePropertySource) locator.locate(environment);

            Object value = propertySource.getProperty("config.client.version");
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("[Config Watch] Cannot fetch version from Config Server", e);
            return null;
        }
    }

    private boolean versionChanged(String oldVersion, String newVersion) {
        return (!hasText(oldVersion) && hasText(newVersion))
                || (hasText(oldVersion) && !oldVersion.equals(newVersion));
    }

    @Override
    public void close() {
        running.compareAndSet(true, false);
    }
}
