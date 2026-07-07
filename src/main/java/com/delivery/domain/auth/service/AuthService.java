package com.delivery.domain.auth.service;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.mapper.UserDtoMapper;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.exception.BusinessException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        User savedUser = createUser(request);
        CustomUserDetails userDetails = CustomUserDetails.from(savedUser);

        String accessToken = jwtUtil.generateAccessToken(userDetails, userDetails.getId());

        return UserDtoMapper.toDto(userDetails, accessToken);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails, userDetails.getId());

        return UserDtoMapper.toDto(userDetails, accessToken);
    }

    private User createUser(SignUpRequestDto request) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CUSTOMER);

        if (Role.OWNER.equals(request.getRole())) {
            roles.add(Role.OWNER);
        }

        return userRepository.save(
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .nickName(request.getNickName())
                        .phoneNumber(request.getPhoneNumber())
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
