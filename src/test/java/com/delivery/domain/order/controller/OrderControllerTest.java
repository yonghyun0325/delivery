package com.delivery.domain.order.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.service.OrderService;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    // Controller 테스트에서는 실제 주문 비즈니스 로직을 실행하지 않음
    @MockitoBean private OrderService orderService;

    // 주문 Controller 테스트와 무관한 외부 Gemini Client는 Mock 처리
    @MockitoBean private GeminiClient geminiClient;

    // 이전 테스트에서 발생한 (OrderService) Mock 호출 기록을 제거하여
    // 각 테스트가 독립적으로 verifyNoInteractions()를 검증할 수 있도록 한다
    @BeforeEach
    void clearOrderServiceInvocations() {
        clearInvocations(orderService);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    /*
     * 테스트용 CustomUserDetails 생성
     *
     * user(...) RequestPostProcessor에 이 객체를 전달하면
     * Controller의 @AuthenticationPrincipal에 같은 객체가 주입된다.
     */
    private CustomUserDetails createUser(Long userId, String role) {
        return CustomUserDetails.builder()
                .id(userId)
                .username(role.toLowerCase() + "1")
                .password("encoded-password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();
    }

    @Nested
    @DisplayName("가게 주문 내역 조회 권한")
    class GetStoreOrdersAuthorizationTest {

        private final UUID storeId = UUID.randomUUID();

        @Test
        @DisplayName("OWNER는 가게 주문 내역 조회 API에 접근할 수 있다")
        void owner_can_access_store_orders() throws Exception {
            // given
            Long ownerId = 10L;
            CustomUserDetails owner = createUser(ownerId, "OWNER");

            // when & then
            mockMvc.perform(get("/api/v1/stores/{storeId}/orders", storeId).with(user(owner)))
                    .andExpect(status().isOk());

            // Controller가 인증 사용자 ID와 역할 목록을 Service에 정확히 전달했는지 확인한다.
            verify(orderService)
                    .getStoreOrders(
                            eq(storeId),
                            eq(ownerId),
                            eq(Set.of("ROLE_OWNER")),
                            isNull(), // startDate
                            isNull(), // endDate
                            isNull(), // status
                            eq(0),
                            eq(10),
                            eq("createdAt,desc"));
        }

        @ParameterizedTest(name = "{0}는 모든 가게 주문 내역 조회 API에 접근할 수 있다")
        @ValueSource(strings = {"MANAGER", "MASTER"})
        void admin_can_access_store_orders(String role) throws Exception {

            // given
            Long adminId = 99L;
            CustomUserDetails admin = createUser(adminId, role);

            // when & then
            mockMvc.perform(get("/api/v1/stores/{storeId}/orders", storeId).with(user(admin)))
                    .andExpect(status().isOk());

            verify(orderService)
                    .getStoreOrders(
                            eq(storeId),
                            eq(adminId),
                            eq(Set.of("ROLE_" + role)),
                            isNull(),
                            isNull(),
                            isNull(),
                            eq(0),
                            eq(10),
                            eq("createdAt,desc"));
        }

        @Test
        @DisplayName("CUSTOMER는 가게 주문 내역 조회 API에 접근할 수 없다")
        void customer_cannot_access_store_orders() throws Exception {
            // given
            CustomUserDetails customer = createUser(100L, "CUSTOMER");

            // when & then
            mockMvc.perform(get("/api/v1/stores/{storeId}/orders", storeId).with(user(customer)))
                    .andExpect(status().isForbidden());

            // @PreAuthorize 단계에서 차단되므로Controller 본문과 Service는 실행되지 않아야 한다.
            verifyNoInteractions(orderService);
        }
    }

    @Nested
    @DisplayName("주문 단건 조회 권한")
    class GetOrderAuthorizationTest {

        private final UUID orderId = UUID.randomUUID();

        @ParameterizedTest(name = "{0}은 주문 단건 조회 API에 접근할 수 있다")
        @ValueSource(strings = {"CUSTOMER", "OWNER", "MANAGER", "MASTER"})
        void allowed_roles_can_access_order_detail(String role) throws Exception {

            // given
            Long currentUserId = 10L;
            CustomUserDetails currentUser = createUser(currentUserId, role);

            // when & then
            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId).with(user(currentUser)))
                    .andExpect(status().isOk());

            /* 실제 본인 주문·본인 가게 여부는 Service 테스트에서 검증한다.
             * Controller 테스트에서는 ID와 역할 전달 여부를 확인한다.
             */
            verify(orderService).getOrder(orderId, currentUserId, Set.of("ROLE_" + role));
        }

        @Test
        @DisplayName("인증 정보가 없으면 주문 단건 조회 API에 접근할 수 없다")
        void unauthenticated_user_cannot_access_order_detail() throws Exception {

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(orderService);
        }
    }

    @Nested
    @DisplayName("가게 주문 상태 변경 권한")
    class ChangeStoreOrderStatusAuthorizationTest {

        private final UUID storeId = UUID.randomUUID();
        private final UUID orderId = UUID.randomUUID();

        // 가게 주문 처리 5개 API와 Service에 전달할 상태값
        static Stream<Arguments> storeStatusChangeCases() {
            return Stream.of(
                    Arguments.of(
                            "/api/v1/stores/{storeId}/orders/{orderId}/reject",
                            OrderStatus.REJECTED),
                    Arguments.of(
                            "/api/v1/stores/{storeId}/orders/{orderId}/accept",
                            OrderStatus.ACCEPTED),
                    Arguments.of(
                            "/api/v1/stores/{storeId}/orders/{orderId}/cook", OrderStatus.COOKING),
                    Arguments.of(
                            "/api/v1/stores/{storeId}/orders/{orderId}/deliver",
                            OrderStatus.DELIVERING),
                    Arguments.of(
                            "/api/v1/stores/{storeId}/orders/{orderId}/delivered",
                            OrderStatus.DELIVERED));
        }

        @ParameterizedTest(name = "OWNER는 {1} 상태 변경 API에 접근할 수 있다")
        @MethodSource("storeStatusChangeCases")
        void owner_can_access_store_status_change_apis(String path, OrderStatus nextStatus)
                throws Exception {

            // given
            Long ownerId = 10L;
            CustomUserDetails owner = createUser(ownerId, "OWNER");

            // when & then
            mockMvc.perform(patch(path, storeId, orderId).with(user(owner)))
                    .andExpect(status().isOk());

            /* URL별로 Controller가 올바른 nextStatus를 선택하여
             * Service에 전달했는지 확인한다.
             */
            verify(orderService)
                    .changeStoreOrderStatus(
                            storeId, orderId, nextStatus, ownerId, Set.of("ROLE_OWNER"));
        }

        @ParameterizedTest(name = "CUSTOMER는 {1} 상태 변경 API에 접근할 수 없다")
        @MethodSource("storeStatusChangeCases")
        void customer_cannot_access_store_status_change_apis(String path, OrderStatus nextStatus)
                throws Exception {
            // given
            CustomUserDetails customer = createUser(100L, "CUSTOMER");

            // when & then
            mockMvc.perform(patch(path, storeId, orderId).with(user(customer)))
                    .andExpect(status().isForbidden());

            /* @PreAuthorize에서 차단되므로
             * Service의 상태 변경 로직은 실행되지 않는다.
             */
            verifyNoInteractions(orderService);
        }

        @ParameterizedTest(name = "{0}는 가게 소유자가 아니어도 상태 변경 API에 접근할 수 있다")
        @ValueSource(strings = {"MANAGER", "MASTER"})
        void admin_can_access_store_status_change_api(String role) throws Exception {

            // given
            Long adminId = 99L;
            CustomUserDetails admin = createUser(adminId, role);

            // 대표 API로 주문 수락 엔드포인트를 사용
            mockMvc.perform(
                            patch(
                                            "/api/v1/stores/{storeId}/orders/{orderId}/accept",
                                            storeId,
                                            orderId)
                                    .with(user(admin)))
                    .andExpect(status().isOk());

            verify(orderService)
                    .changeStoreOrderStatus(
                            storeId,
                            orderId,
                            OrderStatus.ACCEPTED,
                            adminId,
                            Set.of("ROLE_" + role));
        }
    }
}
