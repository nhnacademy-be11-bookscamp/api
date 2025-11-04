package store.bookscamp.api.couponissue.consumer;

import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_QUEUE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.service.CouponIssueService;
import store.bookscamp.api.member.publisher.dto.SignupEventDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupEventConsumer {

    private final CouponIssueService couponIssueService;

    @RabbitListener(queues = SIGNUP_QUEUE)
    public void listenSignupEvent(SignupEventDto dto) {
        try {
            couponIssueService.issueWelcomeCoupon(dto.memberId());
            log.info("회원가입 쿠폰 발행 성공. memberId = {}", dto.memberId());
        } catch (ApplicationException ae) {
            log.info("회원가입 쿠폰 발급 비즈니스 로직 에러.", ae);
        } catch (Exception e) {
            log.info("회원가입 쿠폰 발급 실패. memberId = {}", dto.memberId());
            throw e;
        }
    }
}
