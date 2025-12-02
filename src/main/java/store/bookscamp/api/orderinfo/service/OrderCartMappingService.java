package store.bookscamp.api.orderinfo.service;

import static java.time.Duration.ofMinutes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCartMappingService {

    private static final String ORDER_CART_PREFIX = "orderCart:";
    private static final int TTL_MINUTES = 30;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveMapping(String orderNumber, Long cartId) {
        String key = ORDER_CART_PREFIX + orderNumber;
        redisTemplate.opsForValue().set(key, cartId.toString(), ofMinutes(TTL_MINUTES));
        log.info("[ORDER-CART-MAPPING] 매핑 저장 - orderNumber: {}, cartId: {}", orderNumber, cartId);
    }

    public Long getAndDeleteMapping(String orderNumber) {
        String key = ORDER_CART_PREFIX + orderNumber;
        String cartIdStr = redisTemplate.opsForValue().getAndDelete(key);

        if (cartIdStr == null) {
            log.info("[ORDER-CART-MAPPING] 매핑 없음 - orderNumber: {}", orderNumber);
            return null;
        }

        Long cartId = Long.parseLong(cartIdStr);
        log.info("[ORDER-CART-MAPPING] 매핑 조회 및 삭제 - orderNumber: {}, cartId: {}", orderNumber, cartId);
        return cartId;
    }
}