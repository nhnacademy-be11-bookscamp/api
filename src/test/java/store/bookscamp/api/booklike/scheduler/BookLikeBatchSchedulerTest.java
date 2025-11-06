package store.bookscamp.api.booklike.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.booklike.entity.BookLike;
import store.bookscamp.api.booklike.repository.BookLikeRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Disabled
@SpringBootTest
class BookLikeBatchSchedulerTest {

    @Autowired
    private BookLikeBatchScheduler bookLikeBatchScheduler;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private BookLikeRepository bookLikeRepository;
    @MockitoBean
    private BookRepository bookRepository;
    @MockitoBean
    private MemberRepository memberRepository;

    private HashOperations<String, Object, Object> hashOperations;

    private static final String LIKE_UPDATES_KEY = "like:update";
    private static final String LIKE_COUNT_KEY = "like:count";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        // given
        hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("성공: 신규 좋아요 1건, 좋아요 취소 1건 동기화")
    void flushLikesToDatabase_HappyPath() {
        // given
        String newLikeKey = "1:100";
        String updateLikeKey = "2:101";
        Map<Object, Object> updates = Map.of(
                newLikeKey, "true",
                updateLikeKey, "false"
        );
        when(hashOperations.entries(LIKE_UPDATES_KEY)).thenReturn(updates);

        Book book100 = mock(Book.class);
        Member member1 = mock(Member.class);
        when(bookLikeRepository.findByMemberIdAndBookId(1L, 100L)).thenReturn(null);
        when(bookRepository.findById(100L)).thenReturn(Optional.of(book100));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        BookLike existingLike = new BookLike(mock(Book.class), mock(Member.class), true);
        when(bookLikeRepository.findByMemberIdAndBookId(2L, 101L)).thenReturn(existingLike);

        when(bookLikeRepository.countByBook_IdAndLiked(100L, true)).thenReturn(5L);
        when(bookLikeRepository.countByBook_IdAndLiked(101L, true)).thenReturn(10L);

        when(hashOperations.get(LIKE_UPDATES_KEY, newLikeKey)).thenReturn("true");
        when(hashOperations.get(LIKE_UPDATES_KEY, updateLikeKey)).thenReturn("false");

        // when
        bookLikeBatchScheduler.flushLikesToDatabase();

        // then
        assertThat(existingLike.isLiked()).isFalse();

        ArgumentCaptor<List<BookLike>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(bookLikeRepository, times(1)).saveAll(saveAllCaptor.capture());
        assertThat(saveAllCaptor.getValue()).hasSize(2);

        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, "100", "5");
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, "101", "10");
        verify(redisTemplate, times(1)).persist(LIKE_COUNT_KEY);

        verify(hashOperations, times(1)).delete(LIKE_UPDATES_KEY, newLikeKey);
        verify(hashOperations, times(1)).delete(LIKE_UPDATES_KEY, updateLikeKey);
    }

    @Test
    @DisplayName("처리할 데이터 없음: Redis가 비어있으면 즉시 종료")
    void flushLikesToDatabase_EmptyUpdates() {
        // given
        when(hashOperations.entries(LIKE_UPDATES_KEY)).thenReturn(Map.of());

        // when
        bookLikeBatchScheduler.flushLikesToDatabase();

        // then
        verify(bookLikeRepository, never()).saveAll(any());
        verify(hashOperations, never()).delete(any(), any());
        verify(hashOperations, never()).put(any(), any(), any());
    }

    @Test
    @DisplayName("실패 (레이스 컨디션): 처리 중 값이 변경되면 Redis 키가 삭제되지 않아야 함")
    void flushLikesToDatabase_Fail_RaceCondition() {
        // given
        String raceConditionKey = "1:100";
        Map<Object, Object> updates = Map.of(raceConditionKey, "true");
        when(hashOperations.entries(LIKE_UPDATES_KEY)).thenReturn(updates);

        when(bookLikeRepository.findByMemberIdAndBookId(1L, 100L)).thenReturn(null);
        when(bookRepository.findById(100L)).thenReturn(Optional.of(mock(Book.class)));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mock(Member.class)));

        when(bookLikeRepository.countByBook_IdAndLiked(100L, true)).thenReturn(1L);

        when(hashOperations.get(LIKE_UPDATES_KEY, raceConditionKey)).thenReturn("false");

        // when
        bookLikeBatchScheduler.flushLikesToDatabase();

        // then
        verify(bookLikeRepository, times(1)).saveAll(anyList());
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, "100", "1");
        verify(hashOperations, never()).delete(LIKE_UPDATES_KEY, raceConditionKey);
    }

    @Test
    @DisplayName("실패 (유효하지 않은 데이터): Member가 없으면 DB 저장 및 키 삭제 안 함")
    void flushLikesToDatabase_InvalidData_MemberNotFound() {
        // given
        String invalidKey = "999:100";
        Map<Object, Object> updates = Map.of(invalidKey, "true");
        when(hashOperations.entries(LIKE_UPDATES_KEY)).thenReturn(updates);

        when(bookLikeRepository.findByMemberIdAndBookId(999L, 100L)).thenReturn(null);
        when(bookRepository.findById(100L)).thenReturn(Optional.of(mock(Book.class)));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        bookLikeBatchScheduler.flushLikesToDatabase();

        // then
        verify(bookLikeRepository, never()).saveAll(anyList());
        verify(hashOperations, never()).delete(LIKE_UPDATES_KEY, invalidKey);
    }
}