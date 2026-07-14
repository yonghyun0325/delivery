package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.common.util.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 리프래시 토큰 캐시
 */
@Repository
public class RefreshTokenRepository implements BaseCacheRepository<UUID, String> {
    private final Cache cache;

    public RefreshTokenRepository(CacheManager cacheManager) {

        this.cache = cacheManager.getCache(CacheType.REFRESH_TOKEN.name());
    }

    @Override
    public void save(UUID key, String value) {
        cache.put(key, value);
    }

    @Override
    public Optional<String> findByKey(UUID key) {
        return Optional.ofNullable(cache.get(key, String.class));
    }

    @Override
    public void delete(UUID key) {
        cache.evict(key);
    }
}
