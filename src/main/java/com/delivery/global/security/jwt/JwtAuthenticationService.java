package com.delivery.global.security.jwt;

import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.global.cache.BlackListRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.cache.WithdrawnUserRepository;
import com.delivery.global.security.principal.CustomUserDetails;
import com.delivery.global.security.principal.CustomUserDetailsService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {
    private final UserCacheRepository userCacheRepository;
    private final WithdrawnUserRepository withdrawnUserRepository;
    private final BlackListRepository blackListRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public CustomUserDetails authenticate(String accessToken) {
        UUID userUuid = jwtUtil.getUserUuidFromAccessToken(accessToken);
        UUID sessionId = jwtUtil.getSessionIdFromAccessToken(accessToken);

        // 탈퇴 유저 확인
        if (withdrawnUserRepository.findByKey(userUuid) != null) {
            throw new UserException(UserErrorCode.NOT_EXIST_USER);
        }

        // 블랙 리스트 등록 여부 확인
        if (blackListRepository.findByKey(sessionId) != null) {
            throw new AuthException(AuthErrorCode.BLACKLISTED_TOKEN);
        }

        // User 정보 캐싱
        CustomUserDetails userDetails = userCacheRepository.findByKey(userUuid);
        if (userDetails == null) {
            userDetails = customUserDetailsService.loadUserByUuid(userUuid);
            userCacheRepository.save(userUuid, userDetails);
            log.info("Jwt 캐싱 {} : {}", userUuid, userDetails);
        }

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(accessToken, userDetails)) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        return userDetails;
    }
}
