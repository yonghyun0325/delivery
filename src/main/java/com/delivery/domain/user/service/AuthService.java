package com.delivery.domain.user.service;

import com.delivery.common.base.BaseCacheRepository;
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
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;

import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
public class AuthService {
    private final BaseCacheRepository<UUID, String> refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final BlackListRepository  blackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     * 가입 성공 시 액세스 토큰과 리프래시 토큰 발급
     * @param request 회원 가입 요청 객체
     * @return 로그인 정보 객체
     */
    public AuthResponse signUp(SignUpRequest request) {
        validateDuplicateUsername(request.username());
        validateDuplicateNickName(request.nickName());
        Role.validateSignupRole(request.role());

        String encodedPassword = passwordEncoder.encode(request.password());
        Set<Role> roles = Role.getDefaultRoles(request.role());

        User savedUser =
                userRepository.save(
                        User.create(
                                request.username(),
                                encodedPassword,
                                request.nickName(),
                                request.phoneNumber(),
                                roles));
        CustomUserDetails userDetails = CustomUserDetails.from(savedUser);

        return createAuthResponse(userDetails);
    }

    /**
     * 로그인
     * 사용자 인증 후 액세스 토큰과 리프래시 토큰을 발급
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
     * 로그아웃
     * 리프래시 토큰을 삭제하고
     * 액세스 토큰 블랙리스트에 저장
     * @param request
     */
    public void logout(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveAccessToken(request);

        if (accessToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        UUID userUuid = jwtUtil.getUserUuidFromAccessToken(accessToken);
        refreshTokenRepository.delete(userUuid);
        // 다중 로그인은 필요 없다고 판단
//        blackListRepository.save(accessToken, Boolean.TRUE);
    }

    /**
     * 리프래시 토큰 발급
     * @param request
     * @return
     */
    public AuthResponse refresh(HttpServletRequest request) {
        String refreshToken = jwtUtil.resolveRefreshToken(request);

        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        UUID userUuid = jwtUtil.getUserUuidFromRefreshToken(refreshToken);
        String savedRefreshToken = refreshTokenRepository.findByKey(userUuid)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        validateRefreshToken(refreshToken, savedRefreshToken);

        User user = userRepository.findWithRolesByUserUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));

        CustomUserDetails userDetails = CustomUserDetails.from(user);

        return createAuthResponse(userDetails);
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
     * 액세스 토큰, 리프래시 토큰 성생 후 DTO 반환
     * @param userDetails
     * @return
     */
    private AuthResponse createAuthResponse(CustomUserDetails userDetails) {
        UUID sessionId = UUID.randomUUID();

        String accessToken =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), sessionId);

        String refreshToken = jwtUtil.generateRefreshToken(userDetails, userDetails.getUserUuid(), sessionId);
        refreshTokenRepository.save(userDetails.getUserUuid(), refreshToken);

        return UserDtoMapper.toAuthResponse(userDetails, accessToken, refreshToken);
    }

    private void validateRefreshToken(String refreshToken, String savedToken) {
        if (!savedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
