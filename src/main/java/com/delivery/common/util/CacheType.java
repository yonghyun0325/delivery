package com.delivery.common.util;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 카페인 캐시 타입 */
@Getter
@RequiredArgsConstructor
public enum CacheType {
    REFRESH_TOKEN(Duration.ofDays(14), 10000),
    USER_DETAIL(Duration.ofMinutes(30), 10000),
    BLACK_LIST(Duration.ofMinutes(32), 10000);
    private final Duration ttl;
    private final long maximumSize;
}
