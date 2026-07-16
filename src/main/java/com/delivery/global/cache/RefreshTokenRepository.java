package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.global.config.CacheType;
import java.util.UUID;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

/** 리프래시 토큰 캐시 */
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
    public String findByKey(UUID key) {
        return cache.get(key, String.class);
    }

    @Override
    public void delete(UUID key) {
        cache.evict(key);
    }

    public void deleteAll() {
        cache.clear();
    }
}
