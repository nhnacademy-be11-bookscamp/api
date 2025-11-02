package store.bookscamp.api.cart.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestAsyncConfig {

    @Bean(name = "cartExecutor")
    public Executor cartExecutor() {
        return Runnable::run;
    }
}