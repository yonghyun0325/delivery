package com.delivery.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.io.Serializable;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil implements Serializable {
    @Serial private static final long serialVersionUID = -2634790745690120103L;

    // Acces Token 30분
    public static final long ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30).toMillis();

    // Refresh Token 7일
    public static final long REFRESH_TOKEN_VALIDITY = Duration.ofDays(7).toMillis();

    @Value("${jwt.secret}")
    private String accessSecretKey;

    // TODO : 임시로 JWT키와 동일
    @Value("${jwt.refresh-secret}")
    private String refreshSecretKey;

    public String getUserUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token, Key signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims getAllClaimsFromAccessToken(String token) {
        Key signingKey =
                new SecretKeySpec(
                        Base64.getDecoder().decode(accessSecretKey),
                        SignatureAlgorithm.HS256.getJcaName());
        return getAllClaimsFromToken(token, signingKey);
    }

    public Claims getAllClaimsFromRefreshToken(String token) {
        Key signingKey =
                new SecretKeySpec(
                        Base64.getDecoder().decode(refreshSecretKey),
                        SignatureAlgorithm.HS256.getJcaName());
        return getAllClaimsFromToken(token, signingKey);
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private String generateToken(
            UserDetails userDetails, Long userId, Key signingKey, long validity) {
        Map<String, Object> claims = new HashMap<>();
        Date date = new Date();

        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime() + validity))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(UserDetails userDetails, Long memberId) {
        Key signingKey =
                new SecretKeySpec(
                        Base64.getDecoder().decode(accessSecretKey),
                        SignatureAlgorithm.HS256.getJcaName());
        return generateToken(userDetails, memberId, signingKey, ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(UserDetails userDetails, Long memberId) {
        Key signingKey =
                new SecretKeySpec(
                        Base64.getDecoder().decode(refreshSecretKey),
                        SignatureAlgorithm.HS256.getJcaName());
        return generateToken(userDetails, memberId, signingKey, REFRESH_TOKEN_VALIDITY);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        String username = getUserUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
