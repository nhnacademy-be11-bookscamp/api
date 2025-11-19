package store.bookscamp.api.support;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestRabbitConfig {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        // 테스트에서는 실제로 MQ에 보내지 않고, 목 객체만 사용
        return Mockito.mock(RabbitTemplate.class);
    }
}
