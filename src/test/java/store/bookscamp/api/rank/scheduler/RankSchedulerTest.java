package store.bookscamp.api.rank.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.rank.service.RankService;

@ExtendWith(MockitoExtension.class)
class RankSchedulerTest {

    @InjectMocks
    private RankScheduler rankScheduler;

    @Mock
    private RankService rankService;

    @Test
    @DisplayName("스케줄러 실행 시 회원 등급 업데이트 로직 호출")
    void dailyRankUpdate_success() {
        // when
        rankScheduler.dailyRankUpdate();

        // then
        verify(rankService, times(1)).updateAllMemberGrades();
    }
}