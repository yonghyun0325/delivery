package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/** 블랙리스트 Key : SessionId, Value : 액세스 토큰 */
@Repository
@RequiredArgsConstructor
public class BlackListRepository implements BaseCacheRepository<UUID, String> {
    private final StringRedisTemplate redisTemplate;
    private final CacheType cacheType = CacheType.BLACK_LIST;

    @Override
    public void save(UUID key, String value) {
        redisTemplate.opsForValue().set(generateKey(key), value, cacheType.getTtl());
    }

    @Override
    public String findByKey(UUID key) {
        return redisTemplate.opsForValue().get(generateKey(key));
    }

    @Override
    public void delete(UUID key) {
        redisTemplate.delete(generateKey(key));
    }

    private String generateKey(UUID key) {
        return cacheType.getPrefix() + key.toString();
    }
}
