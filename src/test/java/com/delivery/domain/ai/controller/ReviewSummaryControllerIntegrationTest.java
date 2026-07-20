package com.delivery.domain.ai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.ai.dto.response.ReviewSummaryStatus;
import com.delivery.domain.menu.fixture.StoreTestFixture;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// ReviewSummaryControllerTest(@WebMvcTest, addFilters=false)는 필터 자체가 꺼져있어
// "인증 없이도 됨"이 항상 참으로 나온다 - SecurityConfig의 permitAll이 실제로
// /api/v1/stores/*/review-summary 경로에 걸려있는지는 증명하지 못한다.
// 이 테스트는 필터를 끄지 않고, Authorization 헤더 자체를 아예 안 보내서
// 진짜 공개 API로 동작하는지 확인한다.
// @Transactional로 테스트가 만든 Store 데이터를 종료 시 롤백 - 공유 Testcontainers DB에
// 다른 도메인 테스트와 데이터가 남아 충돌하는 것을 방지한다.
@SpringBootTest(
        properties = {
            "gemini.api-key=test-dummy-key",
            "gemini.base-url=https://generativelanguage.googleapis.com",
            "gemini.model=gemini-1.5-flash"
        })
@AutoConfigureMockMvc
@Transactional
class ReviewSummaryControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private StoreRepository storeRepository;

    @Test
    @DisplayName("인증 헤더 없이 호출해도 200을 반환한다 (permitAll 실제 동작 확인)")
    void getReviewSummary_returns200_withoutAuthorizationHeader() throws Exception {
        Store store = StoreTestFixture.DEFAULT.createStore(600001L);
        UUID storeId = storeRepository.save(store).getStoreId();

        // 의도적으로 Authorization 헤더를 전혀 안 보냄
        mockMvc.perform(get("/api/v1/stores/{storeId}/review-summary", storeId))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value(ReviewSummaryStatus.NOT_ENOUGH_REVIEWS.name()));
    }

    @Test
    @DisplayName("존재하지 않는 가게면 인증 없이도 404를 반환한다(401이 아님)")
    void getReviewSummary_returns404_notUnauthorized_whenStoreMissing() throws Exception {
        mockMvc.perform(get("/api/v1/stores/{storeId}/review-summary", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(StoreErrorCode.STORE_NOT_FOUND.getName()));
    }
}
