package store.bookscamp.api.orderinfo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.bookscamp.api.orderinfo.service.OrderStatusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCompleteScheduler {

    private final OrderStatusService orderStatusService;

    @Scheduled(cron = "0 0 0 * * *")
    public void autoCompleteShippingOrders() {
        log.info("[SCHEDULER] 배송 완료 자동 처리 스케줄러 시작");
        try {
            int completedCount = orderStatusService.autoCompleteShippingOrders();
            log.info("[SCHEDULER] 배송 완료 자동 처리 스케줄러 완료 - 처리 건수: {}", completedCount);
        } catch (Exception e) {
            log.error("[SCHEDULER] 배송 완료 자동 처리 스케줄러 실패", e);
        }
    }
}