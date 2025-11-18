package store.bookscamp.api.common.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable
    ) {
        String name = (String) context.getAttribute("context.name");
        Object method = context.getAttribute("method");

        log.info("재시도 실행... (count: {}), context.name = {}, method = {}",
                context.getRetryCount(),
                name,
                method instanceof java.lang.reflect.Method m ? m.getName() : "unknown"
        );
    }
}
