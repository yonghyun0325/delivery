package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.BlackListRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.global.security.principal.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserCacheRepository userCacheRepository;
    private final BlackListRepository blackListRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 가입 성공 시 액세스 토큰과 리프래시 토큰 발급
     *
     * @param request 회원 가입 요청 객체
     * @return 로그인 정보 객체
     */
    public AuthResponse signUp(SignUpRequest request) {
        validateDuplicateUsername(request.username());
        validateDuplicateNickName(request.nickName());
        Role.validateSignupRole(request.role());

        String encodedPassword = passwordEncoder.encode(request.password());
        Set<Role> roles = Role.getDefaultRoles(request.role());

        try {
            User savedUser =
                    userRepository.saveAndFlush(
                            User.create(
                                    request.username(),
                                    encodedPassword,
                                    request.nickName(),
                                    request.phoneNumber(),
                                    roles));
            CustomUserDetails userDetails = CustomUserDetails.from(savedUser);

            return createAuthResponse(userDetails);
        } catch (DataIntegrityViolationException e) {
            log.warn("AuthService Database Error : {}", e.getMessage());
            throw new UserException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    /**
     * 로그인 사용자 인증 후 액세스 토큰과 리프래시 토큰을 발급
     *
     * @param request
     * @return
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(), request.password()));
        } catch (InternalAuthenticationServiceException | BadCredentialsException e) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return createAuthResponse(userDetails);
    }

    /**
     * 로그아웃 Refresh Token과 User 캐시 삭제 후 블랙리스트 등록
     *
     * @param request
     */
    public void logout(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveAccessToken(request);

        if (accessToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            UUID sessionId = jwtUtil.getSessionIdFromAccessToken(accessToken);
            UUID userUuid = jwtUtil.getUserUuidFromAccessToken(accessToken);

            refreshTokenRepository.delete(sessionId);
            userCacheRepository.delete(userUuid);

            blackListRepository.save(sessionId, accessToken);
        } catch (ExpiredJwtException e) {
            log.info("이미 만료된 토큰으로 로그아웃 시도");
            throw new AuthException(AuthErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    /**
     * 리프래시 토큰 발급
     *
     * @param refreshToken
     * @return
     */
    public AuthResponse refresh(String refreshToken) {
        // 리프래시 토큰 유무 체크
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            UUID userUuid = jwtUtil.getUserUuidFromRefreshToken(refreshToken);
            UUID sessionId = jwtUtil.getSessionIdFromRefreshToken(refreshToken);

            // 블랙리스트에 등록된 세션인지 검증
            if (blackListRepository.findByKey(sessionId) != null) {
                throw new AuthException(AuthErrorCode.BLACKLISTED_TOKEN);
            }

            String savedRefreshToken = refreshTokenRepository.findByKey(sessionId);

            if (savedRefreshToken == null) {
                throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }

            // 캐시에 보관된 토큰과 일치하는지 검증
            validateRefreshToken(refreshToken, savedRefreshToken);

            User user =
                    userRepository
                            .findWithRolesByUserUuidAndDeletedAtIsNull(userUuid)
                            .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));

            CustomUserDetails userDetails = CustomUserDetails.from(user);
            AuthResponse response = createAuthResponse(userDetails);
            refreshTokenRepository.delete(sessionId);

            return response;
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * 회원 중복 아이디 체크
     *
     * @param username
     */
    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    /**
     * 회원 중복 닉네임 체크
     *
     * @param nickName
     */
    private void validateDuplicateNickName(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /**
     * Auth 공통 Response 액세스 토큰, 리프래시 토큰 성생 후 DTO 반환
     *
     * @param userDetails
     * @return
     */
    private AuthResponse createAuthResponse(CustomUserDetails userDetails) {
        try {
            UUID sessionId = UUID.randomUUID();

            String accessToken =
                    jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), sessionId);

            String refreshToken =
                    jwtUtil.generateRefreshToken(userDetails, userDetails.getUserUuid(), sessionId);
            refreshTokenRepository.save(sessionId, refreshToken);

            return UserDtoMapper.toAuthResponse(userDetails, accessToken, refreshToken);
        } catch (Exception e) {
            log.error("토큰 생성 중 오류 발생 {}", e.getMessage());
            throw new AuthException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 리프레시 토큰이 일치하는지 확인
     *
     * @param refreshToken
     * @param savedToken
     */
    private void validateRefreshToken(String refreshToken, String savedToken) {
        if (!savedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
