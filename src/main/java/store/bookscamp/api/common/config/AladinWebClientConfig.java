package store.bookscamp.api.common.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@Slf4j
public class AladinWebClientConfig {

    @Bean(name="aladinClient")
    public WebClient aladinWebClient(@Value("${aladin.base-url}") String baseUrl){
        log.debug(baseUrl);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
