package store.bookscamp.api.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "spring.rabbitmq")
public record RabbitmqProperties(
        String host,
        int port,
        String username,
        String password
) {
}
