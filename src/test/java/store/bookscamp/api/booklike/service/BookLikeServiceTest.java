package store.bookscamp.api.booklike.service;

import org.hibernate.annotations.DialectOverride.SQLDeleteAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;

@SpringBootTest
@Disabled
class BookLikeServiceTest {

    @Autowired
    private BookLikeService likeService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LIKE_UPDATE_KEY = "like:update";

    @DisplayName("toggleLike 통합 테스트 - Redis에 정상 저장되는지 확인")
    @Test
    void toggleLikeTest() {

        // given
        Long memberId = 1L;
        Long bookId = 100L;
        boolean isLiked = true;
        String expectedHashKey = memberId + ":" + bookId;

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        // when
        likeService.toggleLike(memberId, bookId, isLiked);

        // then
        String value = hashOps.get(LIKE_UPDATE_KEY, expectedHashKey);

        org.assertj.core.api.Assertions.assertThat(value).isEqualTo(String.valueOf(isLiked));
    }
}
