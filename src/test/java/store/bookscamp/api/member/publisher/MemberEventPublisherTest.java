package store.bookscamp.api.member.publisher;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_EXCHANGE;
import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_KEY;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.member.publisher.dto.SignupEventDto;

@SpringBootTest
class MemberEventPublisherTest {

    @Autowired
    private MemberEventPublisher memberEventPublisher;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("회원가입 이벤트가 정상적으로 발행된다")
    void publishSignupEvent_success() {

        // given
        Long memberId = 123L;

        // when
        memberEventPublisher.publishSignupEvent(memberId);

        // then: convertAndSend 이 정확히 호출되었는지 검증
        ArgumentCaptor<SignupEventDto> dtoCaptor = ArgumentCaptor.forClass(SignupEventDto.class);

        verify(rabbitTemplate).convertAndSend(
                eq(SIGNUP_EXCHANGE),
                eq(SIGNUP_KEY),
                dtoCaptor.capture()
        );

        SignupEventDto sentDto = dtoCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(sentDto.memberId()).isEqualTo(memberId);
    }
}
