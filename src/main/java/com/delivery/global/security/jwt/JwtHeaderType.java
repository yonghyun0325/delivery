package com.delivery.global.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtHeaderType {
    ACCESS_TOKEN("Authorization"),
    REFRESH_TOKEN("Refresh-Token");

    private final String header;
}
