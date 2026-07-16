package com.delivery.common.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Caffeine CacheManager 설정 Redis로 마이그레이션하는 경우 RedisCacheConfig로 교체 필요 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        for (CacheType cacheType : CacheType.values()) {
            cacheManager.registerCustomCache(
                    cacheType.name(), caffeineCacheBuilder(cacheType).build());
        }

        return cacheManager;
    }

    Caffeine<Object, Object> caffeineCacheBuilder(CacheType cacheType) {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheType.getTtl())
                .maximumSize(cacheType.getMaximumSize())
                .recordStats();
    }
}
