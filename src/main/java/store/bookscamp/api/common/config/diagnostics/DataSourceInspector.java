package store.bookscamp.api.common.config.diagnostics;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceInspector implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("======== [DataSource 확인] ========");
        log.info("DataSource 타입: {}", dataSource.getClass().getName());

        if (dataSource instanceof BasicDataSource ds) {
            log.info("""
                            
                            =========== DBCP 설정 정보 ===========
                            initialSize = {}
                            maxTotal = {}
                            minIdle = {}
                            maxIdle = {}
                            maxWaitMillis = {}
                            validationQuery = {}
                            testOnBorrow = {}
                            testWhileIdle = {}
                            timeBetweenEviction(ms) = {}
                            minEvictableIdle(ms) = {}
                            removeAbandonedOnBorrow = {}
                            removeAbandonedTimeout = {}
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
    }
}
