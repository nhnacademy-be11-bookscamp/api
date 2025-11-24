package store.bookscamp.api.common.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_ISSUE_ALREADY_EXIST;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.WELCOME;
import static store.bookscamp.api.pointpolicy.entity.RewardType.AMOUNT;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.couponissue.service.CouponIssueService;
import store.bookscamp.api.member.publisher.dto.SignupEventDto;
import store.bookscamp.api.pointhistory.service.PointHistoryService;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;

@SpringBootTest
@ActiveProfiles("test")
class SignupEventConsumerTest {

    @Autowired
    private SignupEventConsumer signupEventConsumer;

    @MockitoBean
    private CouponIssueService couponIssueService;

    @MockitoBean
    private PointHistoryService pointHistoryService;

    @MockitoBean
    private PointPolicyRepository pointPolicyRepository;


    @Test
    @DisplayName("정상적으로 포인트 적립 + 쿠폰 발행까지 호출된다")
    void success() {
        // given
        Long memberId = 100L;
        SignupEventDto dto = new SignupEventDto(memberId);

        PointPolicy policy = new PointPolicy(
                WELCOME,
                AMOUNT,
                5000
        );

        when(pointPolicyRepository.findByPointPolicyType(WELCOME))
                .thenReturn(Optional.of(policy));

        // when
        signupEventConsumer.listenSignupEvent(dto);

        // then
        verify(pointHistoryService, times(1))
                .earnPoint(any(PointHistoryEarnDto.class), eq(memberId));

        verify(couponIssueService, times(1))
                .issueWelcomeCoupon(memberId);
    }


    @Test
    @DisplayName("PointPolicy가 없으면 포인트 적립만 실패하고 쿠폰은 정상 발행된다")
    void pointPolicy_not_found() {
        // given
        Long memberId = 200L;
        SignupEventDto dto = new SignupEventDto(memberId);

        when(pointPolicyRepository.findByPointPolicyType(WELCOME))
                .thenReturn(Optional.empty());

        // when
        signupEventConsumer.listenSignupEvent(dto);

        // then
        verify(pointHistoryService, never())
                .earnPoint(any(), anyLong());

        verify(couponIssueService, times(1))
                .issueWelcomeCoupon(memberId);
    }


    @Test
    @DisplayName("쿠폰 발급 시 비즈니스 예외(ApplicationException)이 발생해도 swallow 되고 전체는 정상 동작한다")
    void coupon_issue_business_error() {
        // given
        Long memberId = 300L;
        SignupEventDto dto = new SignupEventDto(memberId);

        PointPolicy policy = new PointPolicy(
                WELCOME,
                AMOUNT,
                5000
        );

        when(pointPolicyRepository.findByPointPolicyType(WELCOME))
                .thenReturn(Optional.of(policy));

        // earnPoint는 정상
        doNothing().when(pointHistoryService).earnPoint(any(), eq(memberId));

        doThrow(new ApplicationException(COUPON_ISSUE_ALREADY_EXIST))
                .when(couponIssueService).issueWelcomeCoupon(memberId);

        // when
        signupEventConsumer.listenSignupEvent(dto);

        // then
        verify(pointHistoryService, times(1)).earnPoint(any(), eq(memberId));
        verify(couponIssueService, times(1)).issueWelcomeCoupon(memberId); // 호출은 됨
    }
}
