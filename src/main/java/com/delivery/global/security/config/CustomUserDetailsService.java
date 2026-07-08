package com.delivery.global.security.config;

import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user =
                userRepository
                        .findWithRolesByUsernameAndDeletedAtIsNull(username)
                        .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_EXIST_USER));

        List<GrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                username,
                user.getPassword(),
                user.getNickName(),
                user.getPhoneNumber(),
                authorities);
    }
}
