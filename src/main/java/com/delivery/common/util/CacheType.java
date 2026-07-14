package com.delivery.common.util;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 카페인 캐시 타입 */
@Getter
@RequiredArgsConstructor
public enum CacheType {
    REFRESH_TOKEN(Duration.ofDays(14), 10000);

//    TODO : 미구현 기능 주석처리
//
//    BLACK_LIST(Duration.ofMinutes(35), 10000),
//
//    EMAIL_AUTH(Duration.ofMinutes(5), 1000),
//
//    PASSWORD_AUTH(Duration.ofMinutes(5), 1000);
    private final Duration ttl;
    private final long maximumSize;
}
