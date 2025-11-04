package store.bookscamp.api.booklike.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import store.bookscamp.api.booklike.entity.BookLike;
import store.bookscamp.api.booklike.repository.BookLikeRepository;
import store.bookscamp.api.booklike.service.dto.BookLikeCountDto;
import store.bookscamp.api.booklike.service.dto.BookLikeStatusDto;

@Service
@RequiredArgsConstructor
public class BookLikeService {

    private static final String LIKE_UPDATE_KEY = "like:update";
    private static final String LIKE_COUNT_KEY = "like:count";

    private final RedisTemplate<String, String> redisTemplate;
    private final BookLikeRepository bookLikeRepository;

    public void toggleLike(Long memberId, Long bookId, boolean isLiked){

        String hashKey = memberId + ":" + bookId;
        String hashValue = String.valueOf(isLiked);
        String bookIdStr = String.valueOf(bookId);

        // 유저 상태 업데이트
        redisTemplate.opsForHash().put(LIKE_UPDATE_KEY, hashKey, hashValue);

        // 총 개수 즉시 업데이트
        long incrementAmount = isLiked ? 1L : -1L;
        redisTemplate.opsForHash().increment(LIKE_COUNT_KEY, bookIdStr, incrementAmount);

        // 키들이 만료되지 않도록 영구 저장 처리
        redisTemplate.persist(LIKE_UPDATE_KEY);
        redisTemplate.persist(LIKE_COUNT_KEY);
    }

    public BookLikeCountDto getLikeCount(Long bookId) {

        String bookIdStr = String.valueOf(bookId);
        String countFromRedis = (String) redisTemplate.opsForHash().get(LIKE_COUNT_KEY, bookIdStr);

        if (countFromRedis != null) {
            long likeCount;
            try {

                likeCount = Long.parseLong(countFromRedis);

                if (likeCount < 0) {
                    // 카운트가 음수면 비정상 상태로 간주, DB에서 다시 읽어옴
                    return getCountFromDBAndCache(bookId, bookIdStr);
                }
                return new BookLikeCountDto(likeCount);

            } catch (NumberFormatException e) {
                // 캐시 값이 숫자가 아닌 경우(비정상), DB에서 다시 읽어옴
                return getCountFromDBAndCache(bookId, bookIdStr);
            }
        }

        // Cache Miss: DB에서 조회 후 캐시에 저장
        return getCountFromDBAndCache(bookId, bookIdStr);
    }

    private BookLikeCountDto getCountFromDBAndCache(Long bookId, String bookIdStr) {
        long countFromDB = bookLikeRepository.countByBook_IdAndLiked(bookId, true);
        String countStr = String.valueOf(countFromDB);

        redisTemplate.opsForHash().put(LIKE_COUNT_KEY, bookIdStr, countStr);

        // 캐시 복구 시에도 영구 저장
        redisTemplate.persist(LIKE_COUNT_KEY);

        return new BookLikeCountDto(countFromDB);
    }

    public BookLikeStatusDto getLikeStatus(Long memberId, Long bookId) {

        String hashKey = memberId + ":" + bookId;
        String statusFromRedis = (String) redisTemplate.opsForHash().get(LIKE_UPDATE_KEY, hashKey);

        if (statusFromRedis != null) {
            boolean liked = Boolean.parseBoolean(statusFromRedis);
            return new BookLikeStatusDto(liked);
        }

        // DB에서 조회
        BookLike bookLike = bookLikeRepository.findByMemberIdAndBookId(memberId, bookId);
        boolean likedFromDB = (bookLike != null && bookLike.isLiked());

        // DB 조회 결과를 다시 Redis에 저장
        redisTemplate.opsForHash().put(LIKE_UPDATE_KEY, hashKey, String.valueOf(likedFromDB));

        // 복구한 캐시도 영구 저장 처리
        redisTemplate.persist(LIKE_UPDATE_KEY);

        // DB 결과 반환
        return new BookLikeStatusDto(likedFromDB);
    }
}