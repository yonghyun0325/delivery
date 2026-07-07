package com.delivery.domain.auth.service;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.mapper.UserDtoMapper;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.exception.BusinessException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public RestApiResponse<AuthResponseDto> signUp(SignUpRequestDto request) {
        validateDuplicateUsername(request.getUsername());
        validateDuplicateNickName(request.getNickName());
        User savedUser = createUser(request);

        // TODO : 엑세스 토큰 발급 미구현
        String accessToken = "test";
        // String accessToken = jwtUtil.generateAccessToken(userDetails, userDetails.getId());
        AuthResponseDto response = UserDtoMapper.toDto(savedUser, accessToken);

        return RestApiResponse.success(HttpStatus.OK, "회원가입 성공", response);
    }

    private User createUser(SignUpRequestDto request) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_CUSTOMER);

        if (Role.ROLE_OWNER.equals(request.getRole())) {
            roles.add(Role.ROLE_OWNER);
        }

        return userRepository.save(
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .nickName(request.getNickName())
                        .tel(request.getPhoneNumber())
                        .roles(roles)
                        .userStatus(UserStatus.ACTIVE)
                        .build());
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateDuplicateNickName(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
