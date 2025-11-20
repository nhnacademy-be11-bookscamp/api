package store.bookscamp.api.rank.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.bookscamp.api.rank.service.RankService;

@Component
@RequiredArgsConstructor
public class RankScheduler {

    private final RankService rankService;

    @Scheduled(cron = "0 0 0 1 * *")
    public void dailyRankUpdate() {
        rankService.updateAllMemberGrades();
    }
}