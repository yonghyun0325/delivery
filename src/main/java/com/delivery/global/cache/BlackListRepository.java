 package com.delivery.global.cache;

 import com.delivery.common.base.BaseCacheRepository;
 import com.delivery.common.util.CacheType;
 import java.util.Optional;
 import org.springframework.cache.Cache;
 import org.springframework.cache.CacheManager;
 import org.springframework.stereotype.Repository;

 /**
  * 토큰 블랙리스트
  */
 @Repository
 public class BlackListRepository implements BaseCacheRepository<String, String> {
    private final Cache cache;

    public BlackListRepository(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CacheType.BLACK_LIST.name());
    }

    @Override
    public void save(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public String findByKey(String key) {
        return cache.get(key, String.class);
    }

    @Override
    public void delete(String key) {
        cache.evict(key);
    }
 }
