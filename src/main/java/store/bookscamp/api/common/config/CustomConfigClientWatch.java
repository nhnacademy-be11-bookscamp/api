package store.bookscamp.api.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
        running.compareAndSet(false, true);
    }

    @Scheduled(
            initialDelayString = "${spring.cloud.config.watch.git.initialDelay:10000}",
            fixedDelayString = "${spring.cloud.config.watch.git.delay:5000}"
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

            log.info("[Config Watch] Context refreshed successfully!");
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
