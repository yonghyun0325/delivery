package com.delivery.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * 카페인 캐시 타입
 */
@Getter
@RequiredArgsConstructor
public enum CacheType {
    REFRESH_TOKEN(
            Duration.ofDays(14),
            10000
    ),

    BLACK_LIST(
            Duration.ofMinutes(35),
            10000
    ),

    EMAIL_AUTH(
            Duration.ofMinutes(5),
            1000
    ),

    PASSWORD_AUTH(
            Duration.ofMinutes(5),
            1000
    );
    private final Duration ttl;
    private final long maximumSize;
}
