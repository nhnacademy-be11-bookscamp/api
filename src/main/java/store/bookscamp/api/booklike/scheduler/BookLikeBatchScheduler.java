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

    @Scheduled(fixedDelay = 1800000)
    public void flushLikesToDatabase() {
        Map<Object, Object> updates = redisTemplate.opsForHash().entries(LIKE_UPDATES_KEY);
        if (updates.isEmpty()) {
            return;
        }

        List<BookLike> likesToSave = new ArrayList<>();
        Set<Long> updatedBookIds = new HashSet<>();
        List<String> successfullyProcessedHashKeys = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : updates.entrySet()) {
            String hashKey = (String) entry.getKey();
            String value = (String) entry.getValue();

            BookLike bookLike = processSingleUpdate(hashKey, value, updatedBookIds);

            if (bookLike != null) {
                likesToSave.add(bookLike);
                successfullyProcessedHashKeys.add(hashKey);
            }
        }

        saveLikesToDatabase(likesToSave, successfullyProcessedHashKeys);

        updateLikeCounts(updatedBookIds);

        cleanupProcessedKeys(updates, successfullyProcessedHashKeys);
    }


    private BookLike processSingleUpdate(String hashKey, String value, Set<Long> updatedBookIds) {
        try {
            String[] keys = hashKey.split(":");
            Long memberId = Long.parseLong(keys[0]);
            Long bookId = Long.parseLong(keys[1]);
            boolean liked = Boolean.parseBoolean(value);

            updatedBookIds.add(bookId);

            BookLike bookLike = bookLikeRepository.findByMemberIdAndBookId(memberId, bookId);

            if (bookLike != null) {
                bookLike.updateLiked(liked);
            } else {
                Book book = bookRepository.findById(bookId).orElse(null);
                Member member = memberRepository.findById(memberId).orElse(null);

                if (book != null && member != null) {
                    bookLike = new BookLike(book, member, liked);
                } else {
                    log.warn("좋아요 스케줄러: 유효하지 않은 책(ID:{}) 또는 회원(ID:{}) 입니다. DB 저장 스킵. HashKey: {}", bookId, memberId, hashKey);
                    return null;
                }
            }
            return bookLike;

        } catch (Exception e) {
            log.error("좋아요 스케줄러 처리 중 오류 발생. HashKey: {}", hashKey, e);
            return null;
        }
    }

    private void saveLikesToDatabase(List<BookLike> likesToSave, List<String> successfullyProcessedHashKeys) {
        if (!likesToSave.isEmpty()) {
            try {
                bookLikeRepository.saveAll(likesToSave);
                log.info("좋아요 {}건 DB 동기화 완료.", likesToSave.size());
            } catch (Exception e) {
                log.error("DB saveAll 실패. '성공' 리스트를 비웁니다 (키 삭제 방지)", e);
                successfullyProcessedHashKeys.clear();
            }
        }
    }

    private void updateLikeCounts(Set<Long> updatedBookIds) {
        if (!updatedBookIds.isEmpty()) {
            for (Long bookId : updatedBookIds) {
                long freshCountFromDB = bookLikeRepository.countByBook_IdAndLiked(bookId, true);
                redisTemplate.opsForHash().put(LIKE_COUNT_KEY, String.valueOf(bookId), String.valueOf(freshCountFromDB));
            }
            redisTemplate.persist(LIKE_COUNT_KEY);
            log.info("좋아요 총 개수 캐시 {}건 갱신 및 영구 저장 완료.", updatedBookIds.size());
        }
    }

    private void cleanupProcessedKeys(Map<Object, Object> originalUpdates, List<String> successfullyProcessedHashKeys) {
        for (String hashKey : successfullyProcessedHashKeys) {
            Object currentValueInRedis = redisTemplate.opsForHash().get(LIKE_UPDATES_KEY, hashKey);
            Object processedValue = originalUpdates.get(hashKey);

            if (currentValueInRedis == null || currentValueInRedis.equals(processedValue)) {
                redisTemplate.opsForHash().delete(LIKE_UPDATES_KEY, hashKey);
            }
        }
    }
}