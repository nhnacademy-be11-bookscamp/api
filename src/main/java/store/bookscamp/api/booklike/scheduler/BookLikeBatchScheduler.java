package store.bookscamp.api.booklike.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.booklike.entity.BookLike;
import store.bookscamp.api.booklike.repository.BookLikeRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookLikeBatchScheduler {

    private static final String LIKE_UPDATES_KEY = "like:update";
    private static final String LIKE_COUNT_KEY = "like:count";

    private final RedisTemplate<String, String> redisTemplate;
    private final BookLikeRepository bookLikeRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    /**
     * 메인 스케줄러 메서드 (리팩토링 후)
     * - 복잡한 로직을 4개의 헬퍼 메서드로 분리하여 '조율' 역할만 수행합니다.
     * - Cognitive Complexity: 8 (목표치 15 이하 달성)
     */
    @Scheduled(fixedDelay = 60000)
    public void flushLikesToDatabase() {
        Map<Object, Object> updates = redisTemplate.opsForHash().entries(LIKE_UPDATES_KEY);
        if (updates.isEmpty()) { // +1
            return;
        }

        List<BookLike> likesToSave = new ArrayList<>();
        Set<Long> updatedBookIds = new HashSet<>();
        List<String> successfullyProcessedHashKeys = new ArrayList<>();

        // 1. Redis 항목 처리 및 DB 저장 목록 생성
        for (Map.Entry<Object, Object> entry : updates.entrySet()) { // +1
            String hashKey = (String) entry.getKey();
            String value = (String) entry.getValue();

            // processSingleUpdate 호출 (+1)
            BookLike bookLike = processSingleUpdate(hashKey, value, updatedBookIds);

            if (bookLike != null) { // +1 (nesting 1)
                likesToSave.add(bookLike);
                successfullyProcessedHashKeys.add(hashKey);
            }
        } // Loop Complexity = 1 + 1 + 2 = 4

        // 2. DB에 일괄 저장 (실패 시 롤백 로직 포함)
        saveLikesToDatabase(likesToSave, successfullyProcessedHashKeys); // +1

        // 3. Redis 좋아요 '총 개수' 캐시 갱신
        updateLikeCounts(updatedBookIds); // +1

        // 4. 처리 완료된 키를 Redis에서 제거 (레이스 컨디션 방어 로직 포함)
        cleanupProcessedKeys(updates, successfullyProcessedHashKeys); // +1
    } // --- 최종 복잡도: 1 + 4 + 1 + 1 + 1 = 8 ---


    /**
     * [HELPER 1] 개별 Redis 항목을 파싱하고 BookLike 엔티티로 변환합니다.
     * - 기존 for문의 try-catch-if-else 로직을 추출했습니다.
     * - Cognitive Complexity: 8
     */
    private BookLike processSingleUpdate(String hashKey, String value, Set<Long> updatedBookIds) {
        try { // +1
            String[] keys = hashKey.split(":");
            Long memberId = Long.parseLong(keys[0]);
            Long bookId = Long.parseLong(keys[1]);
            boolean liked = Boolean.parseBoolean(value);

            updatedBookIds.add(bookId);

            BookLike bookLike = bookLikeRepository.findByMemberIdAndBookId(memberId, bookId);

            if (bookLike != null) { // +1 (nesting 1)
                bookLike.updateLiked(liked);
            } else { // +1 (nesting 1)
                Book book = bookRepository.findById(bookId).orElse(null);
                Member member = memberRepository.findById(memberId).orElse(null);

                if (book != null && member != null) { // +2 (nesting 2) + 1 (&&) = +3
                    bookLike = new BookLike(book, member, liked);
                } else { // +1 (nesting 2)
                    log.warn("좋아요 스케줄러: 유효하지 않은 책(ID:{}) 또는 회원(ID:{}) 입니다. DB 저장 스킵. HashKey: {}", bookId, memberId, hashKey);
                    return null; // 실패 시 null 반환
                }
            }
            return bookLike; // 성공 시 엔티티 반환

        } catch (Exception e) { // +1
            log.error("좋아요 스케줄러 처리 중 오류 발생. HashKey: {}", hashKey, e);
            return null; // 실패 시 null 반환
        }
    }

    /**
     * [HELPER 2] DB에 BookLike 목록을 일괄 저장합니다.
     * - Cognitive Complexity: 4
     */
    private void saveLikesToDatabase(List<BookLike> likesToSave, List<String> successfullyProcessedHashKeys) {
        if (!likesToSave.isEmpty()) { // +1
            try { // +1 (nesting 1)
                bookLikeRepository.saveAll(likesToSave);
                log.info("좋아요 {}건 DB 동기화 완료.", likesToSave.size());
            } catch (Exception e) { // +2 (nesting 1)
                log.error("DB saveAll 실패. '성공' 리스트를 비웁니다 (키 삭제 방지)", e);
                // 키를 삭제하면 안되므로, '성공' 리스트를 강제로 비웁니다.
                successfullyProcessedHashKeys.clear();
            }
        }
    }

    /**
     * [HELPER 3] 변경된 책들의 좋아요 '총 개수'를 DB에서 다시 세어 Redis 캐시를 갱신합니다.
     * - Cognitive Complexity: 3
     */
    private void updateLikeCounts(Set<Long> updatedBookIds) {
        if (!updatedBookIds.isEmpty()) { // +1
            for (Long bookId : updatedBookIds) { // +2 (nesting 1)
                long freshCountFromDB = bookLikeRepository.countByBook_IdAndLiked(bookId, true);
                redisTemplate.opsForHash().put(LIKE_COUNT_KEY, String.valueOf(bookId), String.valueOf(freshCountFromDB));
            }
            redisTemplate.persist(LIKE_COUNT_KEY);
            log.info("좋아요 총 개수 캐시 {}건 갱신 및 영구 저장 완료.", updatedBookIds.size());
        }
    }

    /**
     * [HELPER 4] DB에 성공적으로 반영된 키들만 Redis에서 삭제합니다.
     * - Cognitive Complexity: 3
     */
    private void cleanupProcessedKeys(Map<Object, Object> originalUpdates, List<String> successfullyProcessedHashKeys) {
        // DB에 '성공적'으로 처리된 키들만(successfullyProcessedHashKeys) 삭제 대상으로 삼음
        for (String hashKey : successfullyProcessedHashKeys) { // +1
            Object currentValueInRedis = redisTemplate.opsForHash().get(LIKE_UPDATES_KEY, hashKey);
            // 스케줄러가 읽은 시점(originalUpdates)의 값과 현재 Redis 값이 같은지 비교
            Object processedValue = originalUpdates.get(hashKey);

            // 레이스 컨디션 방지: 스케줄러가 실행되는 동안 값이 변경되지 않은 경우에만 삭제
            if (currentValueInRedis == null || currentValueInRedis.equals(processedValue)) { // +1 (nesting 1) + 1 (||) = +2
                redisTemplate.opsForHash().delete(LIKE_UPDATES_KEY, hashKey);
            }
            // 값이 다르면(레이스 컨디션 발생), 삭제하지 않고 다음 스케줄러가 처리하도록 남겨둠
        }
    }
}