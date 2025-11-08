package store.bookscamp.api.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "store.bookscamp.api.book.repository")
public class ElasticsearchConfig {
}
