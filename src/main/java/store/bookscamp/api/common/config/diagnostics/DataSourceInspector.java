package store.bookscamp.api.common.config.diagnostics;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataSourceInspector implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== [DataSource 확인] ===");
        System.out.println("DataSource 타입: " + dataSource.getClass().getName());

        if (dataSource instanceof BasicDataSource ds) {
            System.out.printf("""
                            initialSize = %d
                            maxTotal = %d
                            minIdle = %d
                            maxIdle = %d
                            maxWaitMillis = %d
                            validationQuery = %s
                            testOnBorrow = %s
                            testWhileIdle = %s
                            timeBetweenEviction(ms) = %d
                            minEvictableIdle(ms) = %d
                            removeAbandonedOnBorrow = %s
                            removeAbandonedTimeout = %d
                            """,
                    ds.getInitialSize(),
                    ds.getMaxTotal(),
                    ds.getMinIdle(),
                    ds.getMaxIdle(),
                    ds.getMaxWaitMillis(),
                    ds.getValidationQuery(),
                    ds.getTestOnBorrow(),
                    ds.getTestWhileIdle(),
                    ds.getTimeBetweenEvictionRunsMillis(),
                    ds.getMinEvictableIdleTimeMillis(),
                    ds.getRemoveAbandonedOnBorrow(),
                    ds.getRemoveAbandonedTimeout()
            );
        }
        System.out.println("=========================================");
    }
}
