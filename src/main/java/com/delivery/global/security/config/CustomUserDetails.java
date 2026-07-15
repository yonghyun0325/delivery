package com.delivery.global.security.config;

import com.delivery.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private UUID userUuid;
    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails from(User user) {
        List<GrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(), user.getUsername(), null, user.getUserUuid(), authorities);
    }

    // 주문 도메인에서 권한 검증할  때 사용하려고 추가했습니다
    /* 현재 로그인 사용자가 가진 권한 이름 목록 반환
     * 예:ROLE_CUSTOMER / ROLE_OWNER / ROLE_MANAGER / ROLE_MASTER */
    public Set<String> getRoleNames() {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
