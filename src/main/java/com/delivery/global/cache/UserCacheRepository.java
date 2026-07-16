package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.global.config.CacheType;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.UUID;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

/**
 * 유저 캐시
 */
@Repository
public class UserCacheRepository implements BaseCacheRepository<UUID, CustomUserDetails> {
    private final Cache cache;

    public UserCacheRepository(CacheManager cacheManager) {

        this.cache = cacheManager.getCache(CacheType.USER_DETAIL.name());
    }

    @Override
    public void save(UUID key, CustomUserDetails value) {
        cache.put(key, value);
    }

    @Override
    public CustomUserDetails findByKey(UUID key) {
        return cache.get(key, CustomUserDetails.class);
    }

    @Override
    public void delete(UUID key) {
        cache.evict(key);
    }

    public void deleteAll() {
        cache.clear();
    }
}
