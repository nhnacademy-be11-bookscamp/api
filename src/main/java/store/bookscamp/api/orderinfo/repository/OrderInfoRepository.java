package store.bookscamp.api.orderinfo.repository;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;

public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {

    // 회원 주문 목록 조회 (페이징 + 정렬은 Pageable에서 처리)
    Page<OrderInfo> findByMemberId(Long orderId, Pageable pageable);

    // TODO : 아직 사용 안함
    Page<OrderInfo> findByMemberIdAndOrderStatusIn(Long orderId,
                                                   Collection<OrderStatus> orderStatus,
                                                   Pageable pageable);
}
