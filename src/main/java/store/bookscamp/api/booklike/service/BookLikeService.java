package store.bookscamp.api.booklike.service;

import io.lettuce.core.RedisCommandExecutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookWishListDto;
import store.bookscamp.api.booklike.entity.BookLike;
import store.bookscamp.api.booklike.repository.BookLikeRepository;
import store.bookscamp.api.booklike.service.dto.BookLikeCountDto;
import store.bookscamp.api.booklike.service.dto.BookLikeStatusDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookLikeService {

    private static final String LIKE_UPDATE_KEY = "like:update";
    private static final String LIKE_COUNT_KEY = "like:count";
    private static final String WISHLIST_MEMBER_KEY_PREFIX = "wishlist:member:";

    private final RedisTemplate<String, String> redisTemplate;
    private final BookLikeRepository bookLikeRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public void toggleLike(Long memberId, Long bookId, boolean isLiked){

        String hashKey = memberId + ":" + bookId;
        String hashValue = String.valueOf(isLiked);
        String bookIdStr = String.valueOf(bookId);
        String wishListKey = WISHLIST_MEMBER_KEY_PREFIX + memberId;

        if (!memberRepository.existsById(memberId)) {
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (!bookRepository.existsById(bookId)) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }
        try {
            // 유저 상태 업데이트
            redisTemplate.opsForHash().put(LIKE_UPDATE_KEY, hashKey, hashValue);

            // 총 개수 즉시 업데이트
            long incrementAmount = isLiked ? 1L : -1L;
            redisTemplate.opsForHash().increment(LIKE_COUNT_KEY, bookIdStr, incrementAmount);

            if (isLiked) {
                redisTemplate.opsForSet().add(wishListKey, bookIdStr);
            } else {
                redisTemplate.opsForSet().remove(wishListKey, bookIdStr);
            }

            // 키들이 만료되지 않도록 영구 저장 처리
            redisTemplate.persist(LIKE_UPDATE_KEY);
            redisTemplate.persist(LIKE_COUNT_KEY);
            redisTemplate.persist(wishListKey);

        } catch (RedisConnectionFailureException e) {
            throw new ApplicationException(ErrorCode.REDIS_CONNECTION_FAILED);
        } catch (RedisCommandExecutionException e) {
            getCountFromDBAndCache(bookId, bookIdStr);
            throw new ApplicationException(ErrorCode.CACHE_DATA_CORRUPTED);
        }
    }

    public BookLikeCountDto getLikeCount(Long bookId) {

        String bookIdStr = String.valueOf(bookId);
        String countFromRedis;

        try {
            countFromRedis = (String) redisTemplate.opsForHash().get(LIKE_COUNT_KEY, bookIdStr);

        } catch (RedisConnectionFailureException e) {
            return getCountFromDBAndCache(bookId, bookIdStr);
        }

        if (countFromRedis != null) {
            long likeCount;
            try {
                likeCount = Long.parseLong(countFromRedis);

                if (likeCount < 0) {
                    return getCountFromDBAndCache(bookId, bookIdStr);
                }
                return new BookLikeCountDto(likeCount);

            } catch (NumberFormatException e) {
                return getCountFromDBAndCache(bookId, bookIdStr);
            }
        }

        return getCountFromDBAndCache(bookId, bookIdStr);
    }

    public BookLikeStatusDto getLikeStatus(Long memberId, Long bookId) {

        String hashKey = memberId + ":" + bookId;
        String statusFromRedis;

        try {
            statusFromRedis = (String) redisTemplate.opsForHash().get(LIKE_UPDATE_KEY, hashKey);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed. Falling back to DB for like status: memberId={}, bookId={}", memberId, bookId);
            return getStatusFromDBAndCache(memberId, bookId, hashKey);
        }

        if (statusFromRedis != null) {
            boolean liked = Boolean.parseBoolean(statusFromRedis);
            return new BookLikeStatusDto(liked);
        }

        return getStatusFromDBAndCache(memberId, bookId, hashKey);
    }

    public List<Book> getWishListByMemberId(Long memberId) {

        String userWishlistKey = WISHLIST_MEMBER_KEY_PREFIX + memberId;
        Set<String> bookIdStrings = redisTemplate.opsForSet().members(userWishlistKey);

        if (bookIdStrings == null || bookIdStrings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> bookIds = bookIdStrings.stream()
                .map(Long::parseLong)
                .toList();

        return bookRepository.findAllById(bookIds);
    }

    public void unlikeBook(Long bookId, Long memberId) {
        String wishListKey = WISHLIST_MEMBER_KEY_PREFIX + memberId;
        String schedulerHashKey = memberId + ":" + bookId;

        redisTemplate.opsForHash().put(LIKE_UPDATE_KEY, schedulerHashKey, "false");

        redisTemplate.opsForSet().remove(wishListKey, bookId.toString());

        redisTemplate.opsForHash().increment(LIKE_COUNT_KEY, String.valueOf(bookId), -1);

        redisTemplate.persist(LIKE_UPDATE_KEY);
        redisTemplate.persist(wishListKey);
        redisTemplate.persist(LIKE_COUNT_KEY);
    }

    private BookLikeCountDto getCountFromDBAndCache(Long bookId, String bookIdStr) {

        long countFromDB = bookLikeRepository.countByBook_IdAndLiked(bookId, true);

        try {
            String countStr = String.valueOf(countFromDB);
            redisTemplate.opsForHash().put(LIKE_COUNT_KEY, bookIdStr, countStr);
            redisTemplate.persist(LIKE_COUNT_KEY);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed during cache write for bookId: {}", bookId);
        }

        return new BookLikeCountDto(countFromDB);
    }

    private BookLikeStatusDto getStatusFromDBAndCache(Long memberId, Long bookId, String hashKey) {

        BookLike bookLike = bookLikeRepository.findByMemberIdAndBookId(memberId, bookId);
        boolean likedFromDB = (bookLike != null && bookLike.isLiked());

        try {
            redisTemplate.opsForHash().put(LIKE_UPDATE_KEY, hashKey, String.valueOf(likedFromDB));
            redisTemplate.persist(LIKE_UPDATE_KEY);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed during cache write for status: memberId={}, bookId={}", memberId, bookId);
        }

        return new BookLikeStatusDto(likedFromDB);
    }
}