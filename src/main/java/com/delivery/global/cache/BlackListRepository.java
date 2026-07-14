package com.delivery.global.cache;

import com.delivery.common.base.BaseCacheRepository;
import com.delivery.common.util.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 토큰 블랙리스트 캐시
 */
@Repository
public class BlackListRepository implements BaseCacheRepository<String, Boolean> {
    private final Cache cache;

    public BlackListRepository(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CacheType.BLACK_LIST.name());
    }

    @Override
    public void save(String key, Boolean value) {
        cache.put(key, value);
    }

    @Override
    public Optional<Boolean> findByKey(String key) {
        return Optional.ofNullable(cache.get(key, Boolean.class));
    }

    @Override
    public void delete(String key) {
        cache.evict(key);
    }
}
