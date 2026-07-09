package com.delivery.domain.auth.service;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.auth.exception.AuthErrorCode;
import com.delivery.domain.auth.exception.AuthException;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.enums.Role;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.mapper.UserDtoMapper;
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
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthResponseDto signUp(SignUpRequestDto request) {
        validateDuplicateUsername(request.getUsername());
        validateDuplicateNickName(request.getNickName());
        Role.validateSignupRole(request.getRole());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Set<Role> roles = Role.getDefaultRoles(request.getRole());

        User savedUser =
                userRepository.save(
                        User.create(
                                request.getUsername(),
                                encodedPassword,
                                request.getNickName(),
                                request.getPhoneNumber(),
                                roles));
        CustomUserDetails userDetails = CustomUserDetails.from(savedUser);

        String accessToken = jwtUtil.generateAccessToken(userDetails, userDetails.getId());

        return UserDtoMapper.toDto(userDetails, accessToken);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUsername(), request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(userDetails, userDetails.getId());

            return UserDtoMapper.toDto(userDetails, accessToken);

        } catch (InternalAuthenticationServiceException | BadCredentialsException e) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN);
        }
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateDuplicateNickName(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
