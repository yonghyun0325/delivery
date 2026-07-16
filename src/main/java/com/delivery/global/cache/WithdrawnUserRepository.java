package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.global.config.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 탈퇴 유저 캐시
 */
@Repository
public class WithdrawnUserRepository implements BaseCacheRepository<UUID, Boolean> {
    private final Cache cache;

    public WithdrawnUserRepository(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CacheType.DRAWN_USER.name());
    }

    @Override
    public void save(UUID key, Boolean value) {
        cache.put(key, value);
    }

    @Override
    public Boolean findByKey(UUID key) {
        return cache.get(key, Boolean.class);
    }

    @Override
    public void delete(UUID key) {
        cache.evict(key);
    }
}
