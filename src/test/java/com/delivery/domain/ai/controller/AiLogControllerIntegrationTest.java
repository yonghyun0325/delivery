package com.delivery.domain.ai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.menu.fixture.TestUserFixture;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.global.security.principal.CustomUserDetails;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// AiLogController는 기존에 테스트가 전혀 없었다. MANAGER/MASTER 전용 API라는 것을
// 실제 @PreAuthorize + 실제 필터 체인으로 검증한다(슬라이스 테스트가 아님).
@SpringBootTest(
        properties = {
            "gemini.api-key=test-dummy-key",
            "gemini.base-url=https://generativelanguage.googleapis.com",
            "gemini.model=gemini-1.5-flash"
        })
@AutoConfigureMockMvc
class AiLogControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    private String issueAccessToken(TestUserFixture fixture) {
        User user = userRepository.save(fixture.createUser());
        CustomUserDetails userDetails = CustomUserDetails.from(user);
        String accessToken =
                jwtUtil.generateAccessToken(userDetails, user.getUserUuid(), UUID.randomUUID());
        refreshTokenRepository.save(user.getUserUuid(), "dummy-refresh-token");
        return accessToken;
    }

    @Test
    @DisplayName("인증 없이 조회하면 401을 반환한다")
    void searchLogs_returns401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/v1/ai-logs")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CUSTOMER는 조회할 수 없다(403)")
    void searchLogs_returns403_whenCustomer() throws Exception {
        String token = issueAccessToken(TestUserFixture.CUSTOMER);

        mockMvc.perform(get("/api/v1/ai-logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWNER는 조회할 수 없다(403)")
    void searchLogs_returns403_whenOwner() throws Exception {
        String token = issueAccessToken(TestUserFixture.OWNER);

        mockMvc.perform(get("/api/v1/ai-logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER는 조회할 수 있다(200)")
    void searchLogs_returns200_whenManager() throws Exception {
        String token = issueAccessToken(TestUserFixture.MANAGER);

        mockMvc.perform(get("/api/v1/ai-logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MASTER는 조회할 수 있다(200)")
    void searchLogs_returns200_whenMaster() throws Exception {
        String token = issueAccessToken(TestUserFixture.MASTER);

        mockMvc.perform(get("/api/v1/ai-logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
