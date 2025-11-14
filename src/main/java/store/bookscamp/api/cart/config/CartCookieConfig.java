package store.bookscamp.api.cart.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import store.bookscamp.api.cart.cookie.CartIdArgumentResolver;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class CartCookieConfig implements WebMvcConfigurer {

    private final CartIdArgumentResolver cartIdArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(cartIdArgumentResolver);
    }
}
