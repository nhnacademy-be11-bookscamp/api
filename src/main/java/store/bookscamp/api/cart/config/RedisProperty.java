package store.bookscamp.api.cart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "spring.data.redis")
public record RedisProperty(
        String host,
        int port
) {
}
