package store.bookscamp.api.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitmqConfig {

    private final RabbitmqProperties rabbitmqProperties;

    public static final String SIGNUP_EXCHANGE = "member.signup.exchange";
    public static final String SIGNUP_QUEUE = "member.signup.queue";
    public static final String SIGNUP_KEY = "member.signup.key";

    @Bean
    public TopicExchange signupTopicExchange() {
        return new TopicExchange(SIGNUP_EXCHANGE, true, false);
    }

    @Bean
    public Queue queue() {
        return new Queue(SIGNUP_QUEUE, true);
    }

    @Bean
    public Binding binding(TopicExchange topicExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(topicExchange).with(SIGNUP_KEY);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitmqProperties.host());
        connectionFactory.setPort(rabbitmqProperties.port());
        connectionFactory.setUsername(rabbitmqProperties.username());
        connectionFactory.setPassword(rabbitmqProperties.password());
        return connectionFactory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
