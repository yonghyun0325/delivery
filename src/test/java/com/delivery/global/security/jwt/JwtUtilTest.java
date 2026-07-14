package com.delivery.global.security.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.delivery.global.config.JwtProperties;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtUtilTest {
    private JwtUtil jwtUtil;
    private CustomUserDetails userDetails;
    private CustomUserDetails userDetails2;

    @BeforeEach
    void setUp() {
        userDetails =
                CustomUserDetails.builder()
                        .id(1L)
                        .username("dummy")
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .userUuid(UUID.randomUUID())
                        .build();

        userDetails2 =
                CustomUserDetails.builder()
                        .id(2L)
                        .username("dummy2")
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .userUuid(UUID.randomUUID())
                        .build();

        JwtProperties jwtConfig =
                new JwtProperties(
                        "PpxDQ9Sl+fPNcE7xfw1nDT3+AwVSiPsY6qHs0IiU864=",
                        "rEvezUuhxfKbs6PZzg13mv9ooenenx8ComFf99A69tw");

        jwtUtil = new JwtUtil(jwtConfig);
    }

    @Test
    @DisplayName("액세스 토큰 생성")
    void createAccessToken() {
        String token =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), UUID.randomUUID());

        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("토큰 정보 확인")
    void getUserUsernameFromToken() {
        String token =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), UUID.randomUUID());

        assertThat(jwtUtil.getUserUsernameFromToken(token)).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("토큰 유효 체크")
    void validateToken() {
        String token =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), UUID.randomUUID());

        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
        assertThat(jwtUtil.validateToken(token, userDetails2)).isFalse();
    }

    @Test
    @DisplayName("액세스 토큰 추출")
    void resolveToken_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token =
                jwtUtil.generateAccessToken(userDetails, userDetails.getUserUuid(), UUID.randomUUID());

        request.addHeader("Authorization", "Bearer " + token);

        var result = jwtUtil.resolveAccessToken(request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("액세스 토큰 헤더 없을 시 null 반환")
    void resolveToken_fail_when_token_is_null() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var result = jwtUtil.resolveAccessToken(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("리프래시 토큰 추출")
    void resolveRefreshToken_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = "refresh-token";

        request.addHeader("Refresh-Token", token);

        var result = jwtUtil.resolveRefreshToken(request);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("리프래시 토큰에서 UUID 추출")
    void getUserUuidFromRefreshToken_success() {
        String token =
                jwtUtil.generateRefreshToken(
                        userDetails,
                        userDetails.getUserUuid(),
                        UUID.randomUUID()
                );

        UUID result = jwtUtil.getUserUuidFromRefreshToken(token);

        assertThat(result).isEqualTo(userDetails.getUserUuid());
    }

    @Test
    @DisplayName("리프래시 토큰 헤더 없을 시 null 반환")
    void resolveRefreshToken_fail_when_token_is_null() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var result = jwtUtil.resolveRefreshToken(request);

        assertThat(result).isNull();
    }
}
