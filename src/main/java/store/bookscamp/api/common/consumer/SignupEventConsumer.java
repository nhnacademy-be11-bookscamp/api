package store.bookscamp.api.common.consumer;

import static store.bookscamp.api.common.config.RabbitmqConfig.SIGNUP_QUEUE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.couponissue.service.CouponIssueService;
import store.bookscamp.api.member.publisher.dto.SignupEventDto;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.PointHistoryService;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupEventConsumer {

    private final CouponIssueService couponIssueService;
    private final PointHistoryService pointHistoryService;
    private final PointPolicyRepository pointPolicyRepository;

    @RabbitListener(queues = SIGNUP_QUEUE)
    public void listenSignupEvent(SignupEventDto dto) {
        try {
            log.info("회원가입 포인트 적립 이벤트 수신. memberId = {}", dto.memberId());

            PointPolicy pointPolicy = pointPolicyRepository
                    .findByPointPolicyType(PointPolicyType.WELCOME)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.POINT_POLICY_NOT_FOUND));

            Integer rewardValue = pointPolicy.getRewardValue();

            PointHistoryEarnDto earnDto = new PointHistoryEarnDto(
                            dto.memberId(),
                            null,
                            PointType.EARN,
                            rewardValue
            );
            pointHistoryService.earnPoint(earnDto);
            log.info("회원가입 포인트 5000점 적립 완료. memberId = {}", dto.memberId());
        } catch (Exception e) {
            log.error("회원가입 포인트 적립 실패. memberId = {}", dto.memberId(), e);
        }

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
