package com.delivery.global.security.jwt;

import static com.delivery.global.security.jwt.JwtHeaderType.ACCESS_TOKEN;
import static com.delivery.global.security.jwt.JwtHeaderType.REFRESH_TOKEN;
import static com.delivery.global.security.jwt.JwtProperties.ACCESS_TOKEN_VALIDITY;
import static com.delivery.global.security.jwt.JwtProperties.REFRESH_TOKEN_VALIDITY;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.io.Serializable;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil implements Serializable {
    @Serial private static final long serialVersionUID = -2634790745690120103L;
    private final JwtProperties jwtProperties;

    public String getUserUsernameFromToken(String token) {
        return getClaimFromAccessToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromAccessToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromAccessToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token, SecretKey signingKey) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public Claims getAllClaimsFromAccessToken(String token) {
        SecretKey signingKey = createSigningKey(jwtProperties.accessSecret());
        return getAllClaimsFromToken(token, signingKey);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private String generateToken(
            UserDetails userDetails, UUID userUuid, UUID sessionId, Key signingKey, long validity) {
        Map<String, Object> claims = new HashMap<>();
        Date date = new Date();

        claims.put("userUuid", userUuid);
        claims.put("sessionId", sessionId);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(date)
                .expiration(new Date(date.getTime() + validity))
                .signWith(signingKey)
                .compact();
    }

    public String generateAccessToken(UserDetails userDetails, UUID userUuid, UUID sessionId) {
        SecretKey signingKey = createSigningKey(jwtProperties.accessSecret());
        return generateToken(userDetails, userUuid, sessionId, signingKey, ACCESS_TOKEN_VALIDITY);
    }

    // Refresh Token
    public Claims getAllClaimsFromRefreshToken(String token) {
        SecretKey signingKey = createSigningKey(jwtProperties.refreshSecret());
        return getAllClaimsFromToken(token, signingKey);
    }

    public String generateRefreshToken(UserDetails userDetails, UUID userUuid, UUID sessionId) {
        SecretKey signingKey = createSigningKey(jwtProperties.refreshSecret());
        return generateToken(userDetails, userUuid, sessionId, signingKey, REFRESH_TOKEN_VALIDITY);
    }

    /**
     * 토큰 검증
     *
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * UUID 추출
     *
     * @param accessToken
     * @return
     */
    public UUID getUserUuidFromAccessToken(String accessToken) {
        Claims claims = getAllClaimsFromAccessToken(accessToken);
        return UUID.fromString(claims.get("userUuid", String.class));
    }

    public UUID getUserUuidFromRefreshToken(String refreshToken) {
        Claims claims = getAllClaimsFromRefreshToken(refreshToken);
        return UUID.fromString(claims.get("userUuid", String.class));
    }

    /**
     * sessionId 추출
     *
     * @param token
     * @return
     */
    public UUID getSessionIdFromAccessToken(String token) {
        Claims claims = getAllClaimsFromAccessToken(token);
        return UUID.fromString(claims.get("sessionId", String.class));
    }

    public UUID getSessionIdFromRefreshToken(String token) {
        Claims claims = getAllClaimsFromRefreshToken(token);
        return UUID.fromString(claims.get("sessionId", String.class));
    }

    /**
     * 토큰 파싱
     *
     * @param request
     * @return
     */
    private String resolveBearerToken(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return resolveBearerToken(request, ACCESS_TOKEN.getHeader());
    }

    // TODO : 로직 변경으로 인한 데드코드
    public String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader(REFRESH_TOKEN.getHeader());
    }

    private SecretKey createSigningKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
