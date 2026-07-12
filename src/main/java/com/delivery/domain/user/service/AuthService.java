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
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;
import java.util.Set;
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
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 회원 가입 후 액세스 토큰을 발급하여 반환
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

        User savedUser =
                userRepository.save(
                        User.create(
                                request.username(),
                                encodedPassword,
                                request.nickName(),
                                request.phoneNumber(),
                                roles));
        CustomUserDetails userDetails = CustomUserDetails.from(savedUser);

        String accessToken =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid().toString());

        return UserDtoMapper.toAuthResponse(userDetails, accessToken);
    }

    /**
     * 로그인 사용자 인증 후 액세스 토큰 발급하여 반환
     *
     * @param request
     * @return
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(), request.password()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken =
                    jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid().toString());

            return UserDtoMapper.toAuthResponse(userDetails, accessToken);

        } catch (InternalAuthenticationServiceException | BadCredentialsException e) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN);
        }
    }

    public void logout() {
        throw new UnsupportedOperationException("개발 중 입니다.");
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
}
