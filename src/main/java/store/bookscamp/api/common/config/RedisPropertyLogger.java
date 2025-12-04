package store.bookscamp.api.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import store.bookscamp.api.common.config.properties.RedisProperty;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class RedisPropertyLogger implements CommandLineRunner {

    private final RedisProperty redisProperty;

    @Override
    public void run(String... args) {
        log.info("""
                        === RedisProperty ===
                        host     = {}
                        port     = {}
                        password = {}
                        database = {}
                        =====================
                        """,
                redisProperty.host(),
                redisProperty.port(),
                redisProperty.password(),
                redisProperty.database()
        );
    }
}
