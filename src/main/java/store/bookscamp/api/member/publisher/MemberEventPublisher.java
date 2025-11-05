package store.bookscamp.api.member.publisher;

import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_EXCHANGE;
import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_KEY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import store.bookscamp.api.member.publisher.dto.SignupEventDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSignupEvent(Long memberId) {
        rabbitTemplate.convertAndSend(
                SIGNUP_EXCHANGE,
                SIGNUP_KEY,
                new SignupEventDto(memberId)
        );
        log.info("회원가입 이벤트 발행 성공");
    }
}
