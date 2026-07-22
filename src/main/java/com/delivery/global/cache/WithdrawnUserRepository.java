package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/** 탈퇴 유저 캐시 */
@Repository
@RequiredArgsConstructor
public class WithdrawnUserRepository {
    private final StringRedisTemplate redisTemplate;
    private final CacheType cacheType = CacheType.WITHDRAWN_USER;

    public void save(UUID key) {
        redisTemplate.opsForValue().set(
                generateKey(key),
                "true",
                cacheType.getTtl()
        );
    }

    public Boolean exists(UUID key) {
        return redisTemplate.hasKey(generateKey(key));
    }

    public void delete(UUID key) {
        redisTemplate.delete(generateKey(key));
    }

    private String generateKey(UUID key) {
        return cacheType.getPrefix() + key.toString();
    }
}
