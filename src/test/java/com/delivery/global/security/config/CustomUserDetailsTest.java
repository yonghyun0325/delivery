package com.delivery.global.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("권한 확인 성공")
    void hasRole_success() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                new SimpleGrantedAuthority("ROLE_OWNER")
        );

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .authorities(authorities)
                .build();

        assertThat(userDetails.hasRole("ROLE_OWNER")).isTrue();   // 가진 경우
        assertThat(userDetails.hasRole("ROLE_CUSTOMER")).isTrue(); // 가진 경우
        assertThat(userDetails.hasRole("ROLE_MANAGER")).isFalse();  // 없는 경우
    }
}