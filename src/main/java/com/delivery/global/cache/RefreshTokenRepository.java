package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/** 리프래시 토큰 캐시 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements BaseCacheRepository<UUID, String> {
    private final StringRedisTemplate redisTemplate;
    private final CacheType cacheType = CacheType.REFRESH_TOKEN;

    @Override
    public void save(UUID key, String value) {
        redisTemplate.opsForValue().set(
                generateKey(key),
                value,
                cacheType.getTtl()
        );
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
