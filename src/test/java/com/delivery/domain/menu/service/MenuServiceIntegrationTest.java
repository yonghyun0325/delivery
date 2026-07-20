package com.delivery.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.domain.ai.repository.AiLogRepository;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;

// GeminiClient(외부 HTTP 경계)만 목으로 막고, MenuService/AiService/AiLogService와 실제
// 트랜잭션 매니저·DB는 전부 진짜로 띄워서 검증한다. 단위 테스트(MenuServiceTest)는
// TransactionTemplate 자체를 목으로 만들어 콜백만 실행하므로, "실제로 커밋/미커밋되는지"는
// 이 테스트가 아니면 증명할 수 없다 - createMenu의 NOT_SUPPORTED(AI 호출을 트랜잭션 밖으로
// 분리)와 AiLogService.saveLog의 REQUIRES_NEW(로그는 호출자 성패와 무관하게 항상 커밋)가
// 실제 스프링 트랜잭션 매니저 위에서 의도대로 동작하는지 확인하는 게 목적.
@SpringBootTest(
        properties = {
            "gemini.api-key=test-dummy-key",
            "gemini.base-url=https://generativelanguage.googleapis.com",
            "gemini.model=gemini-1.5-flash"
        })
class MenuServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MenuService menuService;
    @Autowired private StoreRepository storeRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private AiLogRepository aiLogRepository;

    @MockitoBean private GeminiClient geminiClient;

    private UUID createTestStore(Long ownerId) {
        Store store =
                Store.builder()
                        .userId(ownerId)
                        .categoryId(UUID.randomUUID())
                        .regionId(UUID.randomUUID())
                        .name("트랜잭션검증가게" + UUID.randomUUID())
                        .address("서울시 강남구 테스트로 1")
                        .phone("01012345678")
                        .minOrderAmount(5000)
                        .build();
        return storeRepository.save(store).getStoreId();
    }

    @Test
    @DisplayName("AI 생성 실패 시 메뉴는 저장되지 않고, 실패 로그는 REQUIRES_NEW로 남는다")
    void createMenu_doesNotSaveMenu_butKeepsFailureLog_whenAiFails() {
        Long ownerId = 900001L;
        UUID storeId = createTestStore(ownerId);
        given(geminiClient.generateContent(any())).willThrow(new RestClientException("연결 실패"));

        assertThatThrownBy(
                        () ->
                                menuService.createMenu(
                                        storeId,
                                        "김치찌개",
                                        null,
                                        8000,
                                        true,
                                        "김치찌개 설명 써줘",
                                        ownerId,
                                        false))
                .isInstanceOf(AiException.class);

        // createMenu 자체가 NOT_SUPPORTED라 AI 실패 시 saveMenu가 아예 호출되지 않으므로,
        // 메뉴는 부분 저장/롤백이 아니라 애초에 DB에 쓰인 적이 없어야 한다.
        assertThat(menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId)).isEmpty();

        // AiLogService.saveLog는 REQUIRES_NEW라 호출자(createMenu)가 실패해도 별도로 커밋되어
        // 실패 로그가 실제 DB에 남아있어야 한다.
        boolean hasFailureLog =
                aiLogRepository.findAll().stream()
                        .anyMatch(
                                log ->
                                        log.getRequestType() == AiRequestType.PRODUCT_DESCRIPTION
                                                && !log.isSuccess()
                                                && "연결 실패".equals(log.getErrorMessage()));
        assertThat(hasFailureLog).isTrue();
    }

    @Test
    @DisplayName("AI 생성 성공 시 메뉴가 저장되고 성공 로그도 함께 남는다")
    void createMenu_savesMenu_andSuccessLog_whenAiSucceeds() {
        Long ownerId = 900002L;
        UUID storeId = createTestStore(ownerId);
        given(geminiClient.generateContent(any())).willReturn("AI가 생성한 맛있는 설명");

        MenuResponse response =
                menuService.createMenu(
                        storeId, "된장찌개", null, 9000, true, "된장찌개 설명 써줘", ownerId, false);

        assertThat(response.description()).isEqualTo("AI가 생성한 맛있는 설명");
        assertThat(menuRepository.findByMenuIdAndDeletedAtIsNull(response.menuId())).isPresent();

        boolean hasSuccessLog =
                aiLogRepository.findAll().stream()
                        .anyMatch(
                                log ->
                                        log.getRequestType() == AiRequestType.PRODUCT_DESCRIPTION
                                                && log.isSuccess()
                                                && "AI가 생성한 맛있는 설명".equals(log.getResponseText()));
        assertThat(hasSuccessLog).isTrue();
    }
}
