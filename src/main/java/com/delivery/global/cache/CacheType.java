package com.delivery.global.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 캐시 타입 */
@Getter
@RequiredArgsConstructor
public enum CacheType {
    REFRESH_TOKEN("auth:refresh:", Duration.ofDays(14), 10000),
    USER_DETAIL("auth:userDetail:", Duration.ofMinutes(30), 10000),
    BLACK_LIST("auth:blacklist:", Duration.ofDays(14), 10000),
    WITHDRAWN_USER("auth:withdrawnUser:", Duration.ofMinutes(30), 1000);
    private final String prefix;
    private final Duration ttl;
    private final long maximumSize;
}
