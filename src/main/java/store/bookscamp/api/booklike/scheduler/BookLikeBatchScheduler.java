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

    @Scheduled(fixedDelay = 60000)
    public void flushLikesToDatabase() {

        Map<Object, Object> updates = redisTemplate.opsForHash().entries(LIKE_UPDATES_KEY);
        if (updates.isEmpty()) {
            return;
        }

        List<BookLike> likesToSave = new ArrayList<>();
        Set<Long> updatedBookIds = new HashSet<>();

        // '성공'한 키만 추적하기 위한 리스트
        List<String> successfullyProcessedHashKeys = new ArrayList<>();


        for (Map.Entry<Object, Object> entry : updates.entrySet()) {
            String hashKey = (String) entry.getKey();
            try {
                String[] keys = hashKey.split(":");
                Long memberId = Long.parseLong(keys[0]);
                Long bookId = Long.parseLong(keys[1]);
                boolean liked = Boolean.parseBoolean((String) entry.getValue());

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
                        continue;
                    }
                }
                likesToSave.add(bookLike);

                // DB 저장 리스트에 추가가 '성공'했을 때만 키를 추가
                successfullyProcessedHashKeys.add(hashKey);

            } catch (Exception e) {
                // catch에서는 절대 키를 삭제 리스트에 추가하면 안 됨! (데이터 유실의 원인)
                log.error("좋아요 스케줄러 처리 중 오류 발생. HashKey: {}", hashKey, e);
            }
        }

        // [4. DB 저장 로직]
        if (!likesToSave.isEmpty()) {
            try {
                bookLikeRepository.saveAll(likesToSave);
                log.info("좋아요 {}건 DB 동기화 완료.", likesToSave.size());
            } catch (Exception e) {
                log.error("DB saveAll 실패. '성공' 리스트를 비웁니다 (키 삭제 방지)", e);

                // 키를 삭제하면 안되므로, '성공' 리스트를 강제로 비웁니다.
                successfullyProcessedHashKeys.clear();
            }
        }

        if (!updatedBookIds.isEmpty()) {
            for (Long bookId : updatedBookIds) {
                long freshCountFromDB = bookLikeRepository.countByBook_IdAndLiked(bookId, true);
                redisTemplate.opsForHash().put(LIKE_COUNT_KEY, String.valueOf(bookId), String.valueOf(freshCountFromDB));
            }
            redisTemplate.persist(LIKE_COUNT_KEY);
            log.info("좋아요 총 개수 캐시 {}건 갱신 및 영구 저장 완료.", updatedBookIds.size());
        }

        // DB에 '성공적'으로 처리된 키들만(successfullyProcessedHashKeys) 삭제 대상으로 삼음
        for (String hashKey : successfullyProcessedHashKeys) {
            Object currentValueInRedis = redisTemplate.opsForHash().get(LIKE_UPDATES_KEY, hashKey);
            // 스케줄러가 읽은 시점(updates)의 값과 현재 Redis 값이 같은지 비교
            Object processedValue = updates.get(hashKey);

            if (currentValueInRedis == null || currentValueInRedis.equals(processedValue)) {
                redisTemplate.opsForHash().delete(LIKE_UPDATES_KEY, hashKey);
            }
            // 값이 다르면(레이스 컨디션 발생), 삭제하지 않고 다음 스케줄러가 처리하도록 남겨둠
        }
    }
}