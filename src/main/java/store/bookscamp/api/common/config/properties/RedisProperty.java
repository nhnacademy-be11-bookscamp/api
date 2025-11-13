package store.bookscamp.api.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "spring.data.redis")
public record RedisProperty(
        String host,
        int port,
        String password,
        int database
) {
}