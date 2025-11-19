package store.bookscamp.api.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import store.bookscamp.api.common.config.properties.RedisProperty;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class RedisPropertyLogger implements CommandLineRunner {

    private final RedisProperty redisProperty;

    @Override
    public void run(String... args) {
        System.out.printf("""
                === RedisProperty ===
                host     = %s
                port     = %d
                password = %s
                database = %d
                =====================
                %n""",
                redisProperty.host(),
                redisProperty.port(),
                redisProperty.password(),
                redisProperty.database());
    }
}
