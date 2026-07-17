package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.global.config.CacheType;
import java.util.UUID;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

/** 액세스 토큰 블랙리스트 Key : SessionId Value : 액세스 토큰 */
@Repository
public class BlackListRepository implements BaseCacheRepository<UUID, String> {
    private final Cache cache;

    public BlackListRepository(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CacheType.BLACK_LIST.name());
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
}
