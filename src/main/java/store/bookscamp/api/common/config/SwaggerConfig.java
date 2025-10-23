package store.bookscamp.api.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BooksCamp-OpenAPI")
                        .version("1.0")
                        .description("BooksCamp-OpenAPI 사용법입니다."));
    }

    @Bean
    public GroupedOpenApi api(){
        return null;
    }
}
