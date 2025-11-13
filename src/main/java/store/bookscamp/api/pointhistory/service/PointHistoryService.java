package store.bookscamp.api.pointhistory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryUseDto;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;
    private final OrderInfoRepository orderInfoRepository;


    @Transactional
    public void earnPoint(PointHistoryEarnDto dto, Long memberId) {

        Member member = getMember(memberId);
        OrderInfo orderInfo = getOrderInfoIfPresent(dto.orderId());

        member.earnPoint(dto.pointAmount());

        PointHistory history = PointHistory.earn(orderInfo, member, dto.pointAmount());
        pointHistoryRepository.save(history);
    }

    @Transactional
    public void usePoint(PointHistoryUseDto dto, Long memberId) {

        Member member = getMember(memberId);
        OrderInfo orderInfo = getOrderInfo(dto.orderId());

        member.usePoint(dto.pointAmount());

        PointHistory history = PointHistory.use(orderInfo, member, dto.pointAmount());
        pointHistoryRepository.save(history);
    }

    // 마이페이지 조회
    public List<PointHistory> listMemberPoints(Long memberId) {
        return pointHistoryRepository.findAllHistoryByMemberId(memberId);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private OrderInfo getOrderInfo(Long orderId) {
        return orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
    }

    private OrderInfo getOrderInfoIfPresent(Long orderId) {
        if (orderId == null) {
            return null;
        }

        return getOrderInfo(orderId);
    }
}
