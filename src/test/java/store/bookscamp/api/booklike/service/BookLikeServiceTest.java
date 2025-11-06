package store.bookscamp.api.booklike.service;

import io.lettuce.core.RedisCommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.booklike.entity.BookLike;
import store.bookscamp.api.booklike.repository.BookLikeRepository;
import store.bookscamp.api.booklike.service.dto.BookLikeCountDto;
import store.bookscamp.api.booklike.service.dto.BookLikeStatusDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Disabled
@SpringBootTest
class BookLikeServiceTest {

    @Autowired
    private BookLikeService bookLikeService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private BookLikeRepository bookLikeRepository;
    @MockitoBean
    private MemberRepository memberRepository;
    @MockitoBean
    private BookRepository bookRepository;

    private HashOperations hashOperations;

    private static final String LIKE_UPDATE_KEY = "like:update";
    private static final String LIKE_COUNT_KEY = "like:count";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        // given
        hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("toggleLike - 좋아요 성공")
    void toggleLike_Success_Like() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        boolean isLiked = true;
        String hashKey = memberId + ":" + bookId;
        String bookIdStr = String.valueOf(bookId);

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(bookRepository.existsById(bookId)).thenReturn(true);

        // when
        bookLikeService.toggleLike(memberId, bookId, isLiked);

        // then
        verify(memberRepository, times(1)).existsById(memberId);
        verify(bookRepository, times(1)).existsById(bookId);
        verify(hashOperations, times(1)).put(LIKE_UPDATE_KEY, hashKey, "true");
        verify(hashOperations, times(1)).increment(LIKE_COUNT_KEY, bookIdStr, 1L);
        verify(redisTemplate, times(1)).persist(LIKE_UPDATE_KEY);
        verify(redisTemplate, times(1)).persist(LIKE_COUNT_KEY);
    }

    @Test
    @DisplayName("toggleLike - 좋아요 취소 성공")
    void toggleLike_Success_Unlike() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        boolean isLiked = false;
        String hashKey = memberId + ":" + bookId;
        String bookIdStr = String.valueOf(bookId);

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(bookRepository.existsById(bookId)).thenReturn(true);

        // when
        bookLikeService.toggleLike(memberId, bookId, isLiked);

        // then
        verify(hashOperations, times(1)).put(LIKE_UPDATE_KEY, hashKey, "false");
        verify(hashOperations, times(1)).increment(LIKE_COUNT_KEY, bookIdStr, -1L);
    }

    @Test
    @DisplayName("toggleLike - 멤버 없음 예외 발생")
    void toggleLike_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;
        Long bookId = 10L;
        when(memberRepository.existsById(memberId)).thenReturn(false);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookLikeService.toggleLike(memberId, bookId, true);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        verify(bookRepository, never()).existsById(anyLong());
        verify(hashOperations, never()).put(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("toggleLike - 책 없음 예외 발생")
    void toggleLike_Fail_BookNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 999L;
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookLikeService.toggleLike(memberId, bookId, true);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        verify(hashOperations, never()).put(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("toggleLike - Redis 연결 실패 예외 발생")
    void toggleLike_Fail_RedisConnectionFailed() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(bookRepository.existsById(bookId)).thenReturn(true);

        doThrow(new RedisConnectionFailureException("Connection failed"))
                .when(hashOperations).put(anyString(), anyString(), anyString());

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookLikeService.toggleLike(memberId, bookId, true);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REDIS_CONNECTION_FAILED);
    }

    @Test
    @DisplayName("toggleLike - Redis 명령어 오류 시 DB 조회 및 캐시 복구 시도")
    void toggleLike_Fail_RedisCommandExecution_FallbackToDB() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        boolean isLiked = true;
        String hashKey = memberId + ":" + bookId;
        String hashValue = String.valueOf(isLiked);
        String bookIdStr = String.valueOf(bookId);

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(bookRepository.existsById(bookId)).thenReturn(true);

        doThrow(new RedisCommandExecutionException("Command failed"))
                .when(hashOperations).put(LIKE_UPDATE_KEY, hashKey, hashValue);

        when(bookLikeRepository.countByBook_IdAndLiked(bookId, true)).thenReturn(5L);

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bookLikeService.toggleLike(memberId, bookId, isLiked);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CACHE_DATA_CORRUPTED);

        verify(bookLikeRepository, times(1)).countByBook_IdAndLiked(bookId, true);

        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, bookIdStr, "5");
    }

    @Test
    @DisplayName("getLikeCount - Redis에서 카운트 조회 성공")
    void getLikeCount_Success_FromRedis() {
        // given
        Long bookId = 10L;
        String bookIdStr = String.valueOf(bookId);
        when(hashOperations.get(LIKE_COUNT_KEY, bookIdStr)).thenReturn("123");

        // when
        BookLikeCountDto dto = bookLikeService.getLikeCount(bookId);

        // then
        assertThat(dto.likeCount()).isEqualTo(123L);
        verify(bookLikeRepository, never()).countByBook_IdAndLiked(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("getLikeCount - Redis 연결 실패 시 DB에서 조회 (Fallback)")
    void getLikeCount_Fail_RedisConnectionFailed_FallbackToDB() {
        // given
        Long bookId = 10L;
        String bookIdStr = String.valueOf(bookId);
        doThrow(new RedisConnectionFailureException("Connection failed"))
                .when(hashOperations).get(LIKE_COUNT_KEY, bookIdStr);

        when(bookLikeRepository.countByBook_IdAndLiked(bookId, true)).thenReturn(10L);

        // when
        BookLikeCountDto dto = bookLikeService.getLikeCount(bookId);

        // then
        assertThat(dto.likeCount()).isEqualTo(10L);
        verify(bookLikeRepository, times(1)).countByBook_IdAndLiked(bookId, true);
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, bookIdStr, "10");
    }

    @Test
    @DisplayName("getLikeCount - Redis 캐시 없음 (null) 시 DB에서 조회")
    void getLikeCount_CacheMiss_FallbackToDB() {
        // given
        Long bookId = 10L;
        String bookIdStr = String.valueOf(bookId);
        when(hashOperations.get(LIKE_COUNT_KEY, bookIdStr)).thenReturn(null);
        when(bookLikeRepository.countByBook_IdAndLiked(bookId, true)).thenReturn(5L);

        // when
        BookLikeCountDto dto = bookLikeService.getLikeCount(bookId);

        // then
        assertThat(dto.likeCount()).isEqualTo(5L);
        verify(bookLikeRepository, times(1)).countByBook_IdAndLiked(bookId, true);
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, bookIdStr, "5");
    }

    @Test
    @DisplayName("getLikeCount - Redis 데이터 손상 (Not Number) 시 DB에서 조회")
    void getLikeCount_CacheCorrupted_NotNumber_FallbackToDB() {
        // given
        Long bookId = 10L;
        String bookIdStr = String.valueOf(bookId);
        when(hashOperations.get(LIKE_COUNT_KEY, bookIdStr)).thenReturn("not-a-number");
        when(bookLikeRepository.countByBook_IdAndLiked(bookId, true)).thenReturn(2L);

        // when
        BookLikeCountDto dto = bookLikeService.getLikeCount(bookId);

        // then
        assertThat(dto.likeCount()).isEqualTo(2L);
        verify(bookLikeRepository, times(1)).countByBook_IdAndLiked(bookId, true);
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, bookIdStr, "2");
    }

    @Test
    @DisplayName("getLikeCount - Redis 데이터 손상 (음수) 시 DB에서 조회")
    void getLikeCount_CacheCorrupted_Negative_FallbackToDB() {
        // given
        Long bookId = 10L;
        String bookIdStr = String.valueOf(bookId);
        when(hashOperations.get(LIKE_COUNT_KEY, bookIdStr)).thenReturn("-1");
        when(bookLikeRepository.countByBook_IdAndLiked(bookId, true)).thenReturn(3L);

        // when
        BookLikeCountDto dto = bookLikeService.getLikeCount(bookId);

        // then
        assertThat(dto.likeCount()).isEqualTo(3L);
        verify(bookLikeRepository, times(1)).countByBook_IdAndLiked(bookId, true);
        verify(hashOperations, times(1)).put(LIKE_COUNT_KEY, bookIdStr, "3");
    }


    @Test
    @DisplayName("getLikeStatus - Redis에서 상태 조회 성공 (true)")
    void getLikeStatus_Success_FromRedis_True() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        String hashKey = memberId + ":" + bookId;
        when(hashOperations.get(LIKE_UPDATE_KEY, hashKey)).thenReturn("true");

        // when
        BookLikeStatusDto dto = bookLikeService.getLikeStatus(memberId, bookId);

        // then
        assertThat(dto.liked()).isTrue();
        verify(bookLikeRepository, never()).findByMemberIdAndBookId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getLikeStatus - Redis에서 상태 조회 성공 (false)")
    void getLikeStatus_Success_FromRedis_False() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        String hashKey = memberId + ":" + bookId;
        when(hashOperations.get(LIKE_UPDATE_KEY, hashKey)).thenReturn("false");

        // when
        BookLikeStatusDto dto = bookLikeService.getLikeStatus(memberId, bookId);

        // then
        assertThat(dto.liked()).isFalse();
        verify(bookLikeRepository, never()).findByMemberIdAndBookId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getLikeStatus - Redis 연결 실패 시 DB에서 조회 (Fallback)")
    void getLikeStatus_Fail_RedisConnectionFailed_FallbackToDB() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        String hashKey = memberId + ":" + bookId;
        doThrow(new RedisConnectionFailureException("Connection failed"))
                .when(hashOperations).get(LIKE_UPDATE_KEY, hashKey);

        BookLike mockBookLike = mock(BookLike.class);
        when(mockBookLike.isLiked()).thenReturn(true);
        when(bookLikeRepository.findByMemberIdAndBookId(memberId, bookId)).thenReturn(mockBookLike);

        // when
        BookLikeStatusDto dto = bookLikeService.getLikeStatus(memberId, bookId);

        // then
        assertThat(dto.liked()).isTrue();
        verify(bookLikeRepository, times(1)).findByMemberIdAndBookId(memberId, bookId);
        verify(hashOperations, times(1)).put(LIKE_UPDATE_KEY, hashKey, "true");
    }

    @Test
    @DisplayName("getLikeStatus - Redis 캐시 없음 (null) 시 DB에서 조회 (DB: false)")
    void getLikeStatus_CacheMiss_FallbackToDB_False() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        String hashKey = memberId + ":" + bookId;
        when(hashOperations.get(LIKE_UPDATE_KEY, hashKey)).thenReturn(null);

        when(bookLikeRepository.findByMemberIdAndBookId(memberId, bookId)).thenReturn(null);

        // when
        BookLikeStatusDto dto = bookLikeService.getLikeStatus(memberId, bookId);

        // then
        assertThat(dto.liked()).isFalse();
        verify(bookLikeRepository, times(1)).findByMemberIdAndBookId(memberId, bookId);
        verify(hashOperations, times(1)).put(LIKE_UPDATE_KEY, hashKey, "false");
    }
}