package store.bookscamp.api.pointhistory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;

import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;

import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryUseDto;

@SpringBootTest
class PointHistoryServiceTest {

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    OrderInfoRepository orderInfoRepository;

    @InjectMocks
    PointHistoryService service;

    private Member createMember() {
        return new Member(
                "홍길동",
                "pw",
                "email@test.com",
                "01012345678",
                1000,
                null,
                MemberStatus.NORMAL,
                LocalDate.now(),
                "user1",
                LocalDateTime.now(),
                LocalDate.of(1999,1,1)
        );
    }

    private OrderInfo createOrder(Member m) {
        return new OrderInfo(
                "ORD123",
                m,
                null,
                null,
                10000,
                12000,
                3000,
                1000,
                0,
                12000,
                OrderStatus.PENDING,
                0
        );
    }

    @Test
    @DisplayName("earnPoint 성공")
    void earn_success() {

        Member m = createMember();
        OrderInfo order = createOrder(m);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));
        when(orderInfoRepository.findById(10L)).thenReturn(Optional.of(order));

        PointHistoryEarnDto dto = new PointHistoryEarnDto(
                1L, 10L, PointType.EARN, 500, "적립"
        );

        service.earnPoint(dto, 1L);

        verify(pointHistoryRepository).save(any(PointHistory.class));
        assertThat(m.getPoint()).isEqualTo(1500);
    }

    @Test
    @DisplayName("earnPoint - orderId null → 주문 없이 적립")
    void earn_no_order() {

        Member m = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));

        PointHistoryEarnDto dto = new PointHistoryEarnDto(
                1L, null, PointType.EARN, 300, "적립"
        );

        service.earnPoint(dto, 1L);

        verify(pointHistoryRepository).save(any());
        assertThat(m.getPoint()).isEqualTo(1300);
    }

    @Test
    @DisplayName("earnPoint 실패 - Member 미존재")
    void earn_member_not_found() {

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        PointHistoryEarnDto dto = new PointHistoryEarnDto(
                1L, null, PointType.EARN, 200, "적립"
        );

        assertThatThrownBy(() -> service.earnPoint(dto, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("earnPoint 실패 - 주문 없음")
    void earn_order_not_found() {

        Member m = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));
        when(orderInfoRepository.findById(99L)).thenReturn(Optional.empty());

        PointHistoryEarnDto dto = new PointHistoryEarnDto(
                1L, 99L, PointType.EARN, 200, "적립"
        );

        assertThatThrownBy(() -> service.earnPoint(dto, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("usePoint 성공")
    void use_success() {

        Member m = createMember();
        OrderInfo order = createOrder(m);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));
        when(orderInfoRepository.findById(10L)).thenReturn(Optional.of(order));

        PointHistoryUseDto dto = new PointHistoryUseDto(
                1L, 10L, PointType.USE, 400, "사용"
        );

        service.usePoint(dto, 1L);

        verify(pointHistoryRepository).save(any());
        assertThat(m.getPoint()).isEqualTo(600);
    }

    @Test
    @DisplayName("usePoint 실패 - member 없음")
    void use_member_not_found() {

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        PointHistoryUseDto dto = new PointHistoryUseDto(
                1L, 10L, PointType.USE, 100, "사용"
        );

        assertThatThrownBy(() -> service.usePoint(dto, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("usePoint 실패 - 주문 없음")
    void use_order_not_found() {

        Member m = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));
        when(orderInfoRepository.findById(10L)).thenReturn(Optional.empty());

        PointHistoryUseDto dto = new PointHistoryUseDto(
                1L, 10L, PointType.USE, 200, "사용"
        );

        assertThatThrownBy(() -> service.usePoint(dto, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("usePoint 실패 - 포인트 부족")
    void use_not_enough_point() {

        Member m = createMember();
        m.usePoint(900); // 남은 포인트 = 100

        OrderInfo order = createOrder(m);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));
        when(orderInfoRepository.findById(10L)).thenReturn(Optional.of(order));

        PointHistoryUseDto dto = new PointHistoryUseDto(
                1L, 10L, PointType.USE, 200, "사용"
        );

        assertThatThrownBy(() -> service.usePoint(dto, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_POINT.getMessage());
    }

    @Test
    @DisplayName("listMemberPoints - 정렬 포함 정상조회")
    void list_success() {

        PageRequest sorted = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        PointHistory ph = new PointHistory(null, null, PointType.EARN, 100, "적립");

        when(pointHistoryRepository.findAllHistoryByMemberId(1L, sorted))
                .thenReturn(new PageImpl<>(List.of(ph)));

        Page<PointHistory> result =
                service.listMemberPoints(1L, PageRequest.of(0,10));

        List<PointHistory> list = result.getContent();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPointAmount()).isEqualTo(100);
    }
}
