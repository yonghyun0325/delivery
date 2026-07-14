package com.delivery.domain.order.service;

import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.dto.response.OrderListResponse;
import com.delivery.domain.order.dto.response.OrderStatusResponse;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.global.exception.BusinessException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @Mock private StoreRepository storeRepository;

    @Mock private MenuService menuService;

    @Mock private PaymentService paymentService;

    @InjectMocks private OrderService orderService;

    private UUID storeId;
    private UUID otherStoreId;

    private Long ownerId;
    private Long otherOwnerId;
    private Long customerId;
    private Long otherCustomerId;

    @BeforeEach
    void setUp() {

        storeId = UUID.randomUUID();
        otherStoreId = UUID.randomUUID();

        ownerId = 10L;
        otherOwnerId = 20L;
        customerId = 100L;
        otherCustomerId = 200L;
    }

    /* 가게 주문 내역 조회 권한 테스트
     * OWNER:
     * - 본인 가게만 조회 가능
     * MANAGER / MASTER:
     * - 가게 소유자와 관계없이 조회 가능
     */
    @Nested
    @DisplayName("가게 주문 내역 조회 권한")
    class GetStoreOrdersAccessTest {

        @Test
        @DisplayName("OWNER는 본인 가게의 주문 내역을 조회할 수 있다")
        void owner_can_get_own_store_orders() {
            // given
            Store store = createStore(storeId, ownerId);

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(store));

            // 실제 주문 데이터가 없어도 권한 검증 흐름은 확인할 수 있도록 빈 페이지 반환
            given(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            Set<String> roles = Set.of("ROLE_OWNER");

            // when
            OrderListResponse response =
                    orderService.getStoreOrders(
                            storeId, ownerId, roles, null, null, null, 0, 10, "createdAt,desc");

            // then
            assertThat(response).isNotNull();

            // 본인 가게이므로 예외 없이 Repository 검색까지 실행되어야 한다.
            verify(storeRepository).findByStoreIdAndDeletedAtIsNull(storeId);

            verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("OWNER는 다른 OWNER의 가게 주문 내역을 조회할 수 없다")
        void owner_cannot_get_other_owner_store_orders() {
            // given
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            Set<String> roles = Set.of("ROLE_OWNER");

            // when & then
            assertThatThrownBy(
                            () ->
                                    orderService.getStoreOrders(
                                            storeId,
                                            ownerId,
                                            roles,
                                            null,
                                            null,
                                            null,
                                            0,
                                            10,
                                            "createdAt,desc"))
                    .isInstanceOf(BusinessException.class);

            // 소유권 검증에서 실패하므로 주문 검색 Repository까지 실행되면 안 된다.
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("MANAGER는 가게 소유자 ID가 달라도 주문 내역을 조회할 수 있다")
        void manager_can_get_any_store_orders() {
            // given
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            given(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            Set<String> roles = Set.of("ROLE_MANAGER");

            // when & then
            assertThatCode(
                            () ->
                                    orderService.getStoreOrders(
                                            storeId,
                                            ownerId,
                                            roles,
                                            null,
                                            null,
                                            null,
                                            0,
                                            10,
                                            "createdAt,desc"))
                    .doesNotThrowAnyException();

            verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("MASTER는 가게 소유자 ID가 달라도 주문 내역을 조회할 수 있다")
        void master_can_get_any_store_orders() {
            // given
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            given(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            Set<String> roles = Set.of("ROLE_MASTER");

            // when & then
            assertThatCode(
                            () ->
                                    orderService.getStoreOrders(
                                            storeId,
                                            ownerId,
                                            roles,
                                            null,
                                            null,
                                            null,
                                            0,
                                            10,
                                            "createdAt,desc"))
                    .doesNotThrowAnyException();
        }
    }

    /* 주문 단건 조회 권한 테스트
     * CUSTOMER:
     * - 본인 주문만 조회 가능
     * OWNER:
     * - 본인 가게 주문만 조회 가능
     * MANAGER / MASTER:
     * - 전체 주문 조회 가능
     */
    @Nested
    @DisplayName("주문 단건 조회 권한")
    class GetOrderAccessTest {

        @Test
        @DisplayName("CUSTOMER는 본인이 생성한 주문을 조회할 수 있다")
        void customer_can_get_own_order() {
            // given
            Order order = createOrder(customerId, storeId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            Set<String> roles = Set.of("ROLE_CUSTOMER");

            // when
            OrderDetailResponse response = orderService.getOrder(order.getId(), customerId, roles);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("CUSTOMER는 다른 고객의 주문을 조회할 수 없다")
        void customer_cannot_get_other_customer_order() {
            // given
            Order order = createOrder(otherCustomerId, storeId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            Set<String> roles = Set.of("ROLE_CUSTOMER");

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(order.getId(), customerId, roles))
                    .isInstanceOf(BusinessException.class);

            // CUSTOMER 접근 검증에서는 Store 조회가 필요하지 않다.
            verifyNoInteractions(storeRepository);
        }

        @Test
        @DisplayName("OWNER는 본인 가게의 주문을 조회할 수 있다")
        void owner_can_get_own_store_order() {
            // given
            Order order = createOrder(customerId, storeId);
            Store store = createStore(storeId, ownerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(store));

            Set<String> roles = Set.of("ROLE_OWNER");

            // when
            OrderDetailResponse response = orderService.getOrder(order.getId(), ownerId, roles);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("OWNER는 다른 OWNER의 가게 주문을 조회할 수 없다")
        void owner_cannot_get_other_owner_store_order() {
            // given
            Order order = createOrder(customerId, storeId);
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            Set<String> roles = Set.of("ROLE_OWNER");

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(order.getId(), ownerId, roles))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("MANAGER는 소유권과 관계없이 주문을 조회할 수 있다")
        void manager_can_get_any_order() {
            // given
            Order order = createOrder(customerId, storeId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            Set<String> roles = Set.of("ROLE_MANAGER");

            // when & then
            assertThatCode(() -> orderService.getOrder(order.getId(), 999L, roles))
                    .doesNotThrowAnyException();

            // MANAGER는 전체 주문 조회가 가능하므로 Store 소유권 조회가 실행되지 않아야 한다.
            verifyNoInteractions(storeRepository);
        }

        @Test
        @DisplayName("MASTER는 소유권과 관계없이 주문을 조회할 수 있다")
        void master_can_get_any_order() {
            // given
            Order order = createOrder(customerId, storeId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            Set<String> roles = Set.of("ROLE_MASTER");

            // when & then
            assertThatCode(() -> orderService.getOrder(order.getId(), 999L, roles))
                    .doesNotThrowAnyException();

            verifyNoInteractions(storeRepository);
        }
    }

    /* 가게 주문 상태 변경 권한 테스트
     * OWNER:
     * - 본인 가게 주문만 상태 변경 가능
     * MANAGER / MASTER:
     * - 가게 소유자와 관계없이 변경 가능
     */
    @Nested
    @DisplayName("가게 주문 상태 변경 권한")
    class ChangeStoreOrderStatusAccessTest {

        @Test
        @DisplayName("OWNER는 본인 가게 주문을 수락할 수 있다")
        void owner_can_accept_own_store_order() {
            // given
            Order order = createOrder(customerId, storeId);
            Store store = createStore(storeId, ownerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(store));

            Set<String> roles = Set.of("ROLE_OWNER");

            // when
            OrderStatusResponse response =
                    orderService.changeStoreOrderStatus(
                            storeId, order.getId(), OrderStatus.ACCEPTED, ownerId, roles);

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.ACCEPTED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("OWNER는 다른 OWNER 가게 주문 상태를 변경할 수 없다")
        void owner_cannot_change_other_owner_store_order() {
            // given
            Order order = createOrder(customerId, storeId);
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            Set<String> roles = Set.of("ROLE_OWNER");

            // when & then
            assertThatThrownBy(
                            () ->
                                    orderService.changeStoreOrderStatus(
                                            storeId,
                                            order.getId(),
                                            OrderStatus.ACCEPTED,
                                            ownerId,
                                            roles))
                    .isInstanceOf(BusinessException.class);

            // 검증 실패로 실제 상태는 변경되지 않아야 한다.
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REQUESTED);
        }

        @Test
        @DisplayName("MANAGER는 가게 소유자가 아니어도 주문 상태를 변경할 수 있다")
        void manager_can_change_any_store_order_status() {
            // given
            Order order = createOrder(customerId, storeId);
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            Set<String> roles = Set.of("ROLE_MANAGER");

            // when
            OrderStatusResponse response =
                    orderService.changeStoreOrderStatus(
                            storeId, order.getId(), OrderStatus.ACCEPTED, 999L, roles);

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("MASTER는 가게 소유자가 아니어도 주문 상태를 변경할 수 있다")
        void master_can_change_any_store_order_status() {
            // given
            Order order = createOrder(customerId, storeId);
            Store otherOwnerStore = createStore(storeId, otherOwnerId);

            given(orderRepository.findByIdAndDeletedAtIsNull(order.getId()))
                    .willReturn(Optional.of(order));

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .willReturn(Optional.of(otherOwnerStore));

            Set<String> roles = Set.of("ROLE_MASTER");

            // when
            OrderStatusResponse response =
                    orderService.changeStoreOrderStatus(
                            storeId, order.getId(), OrderStatus.ACCEPTED, 999L, roles);

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.ACCEPTED);
        }
    }

    // 테스트용 Store 생성 메서드
    // Store는 Builder를 제공하므로 테스트에 필요한 값만 설정한다.
    private Store createStore(UUID storeId, Long ownerId) {
        return Store.builder()
                .storeId(storeId)
                .userId(ownerId)
                .categoryId(UUID.randomUUID())
                .regionId(UUID.randomUUID())
                .name("테스트 가게")
                .address("서울시 테스트 주소")
                .phone("01012345678")
                .description("테스트 가게입니다.")
                .minOrderAmount(10000)
                .isOpen(true)
                .averageRating(0.0)
                .build();
    }

    // 테스트용 Order 생성 메서드
    // Order 생성자는 기본 상태를 REQUESTED로 설정한다.
    private Order createOrder(Long userId, UUID storeId) {
        return new Order(userId, storeId, "서울시 테스트 배송지");
    }
}
