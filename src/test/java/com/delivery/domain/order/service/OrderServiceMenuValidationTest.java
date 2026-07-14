package com.delivery.domain.order.service;

import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.request.OrderItemCreateRequest;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.order.exception.OrderException;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceMenuValidationTest {
    // 주문 생성 중 메뉴 검증 로직 테스트
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private OrderService orderService;

    private UUID storeId;
    private UUID menuId;
    private Long currentUserId;

    private Store store;

    private OrderCreateRequest request;

    // given
    @BeforeEach
    void setUp() {
        // 테스트에서 공통으로 사용할 식별자
        storeId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        currentUserId = 1L;

        // 메뉴 검증 전에 필요한 가게 정보를 Mock 객체로 사용
        store = mock(Store.class);

        // 기본 주문 요청: 메뉴 1개를 1개 주문
        request = createOrderRequest(1);

        // 메뉴 검증 테스트가 실행되려면
        // 먼저 가게 존재 및 영업 여부 검증을 통과해야 함
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(Optional.of(store));

        when(store.getIsOpen()).thenReturn(true);
    }

    // 주문 생성시 메뉴가 없는 경우
    @Test
    @DisplayName("주문 생성 시 메뉴가 존재하지 않으면 MENU_NOT_FOUND 예외가 발생한다")
    void createOrder_menuNotFound() {

        // given
        when(menuService.getOrderableMenu(menuId, storeId))
                .thenThrow(
                        new MenuException(MenuErrorCode.MENU_NOT_FOUND)
                );

        // when & then
        assertThatThrownBy(() ->
                orderService.createOrder(request, currentUserId)
        )
                .isInstanceOf(MenuException.class)
                .hasMessage(MenuErrorCode.MENU_NOT_FOUND.getMessage());

        // OrderService가 MenuService에 메뉴 ID와 가게 ID를 올바르게 전달했는지 확인
        verify(menuService)
                .getOrderableMenu(menuId, storeId);

        // 메뉴 조회 단계에서 실패했으므로 주문은 저장되지 않아야 함
        verify(orderRepository, never()).save(any(Order.class));
    }


    // 정상 메뉴 주문 생성
    @Test
    @DisplayName("주문 가능한 메뉴 스냅샷으로 주문이 생성된다")
    void createOrder_success() {

        // given
        int quantity = 2;
        int menuPrice = 12_000;

        // 정상 케이스는 수량 2개로 별도 요청 생성
        OrderCreateRequest successRequest = createOrderRequest(quantity);

        when(store.getMinOrderAmount()).thenReturn(10_000);

        // MenuService가 주문 가능 여부를 검증한 메뉴 스냅샷 반환
        when(menuService.getOrderableMenu(menuId, storeId))
                .thenReturn(
                        new MenuSnapshot(
                                menuId,
                                "불고기 피자",
                                menuPrice
                        )
                );

        // save()로 전달된 Order 객체를 그대로 반환
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        orderService.createOrder(successRequest, currentUserId);

        // then
        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        // MenuService에 menuId와 storeId가 올바른 순서로 전달됐는지 확인
        verify(menuService).getOrderableMenu(menuId, storeId);

        Order savedOrder = orderCaptor.getValue();

        // 주문 기본 정보 확인
        assertThat(savedOrder.getUserId()).isEqualTo(currentUserId);
        assertThat(savedOrder.getStoreId()).isEqualTo(storeId);
        assertThat(savedOrder.getDeliveryAddress()).isEqualTo("서울시 강남구");

        // 총 주문 금액 = 메뉴 가격 × 수량
        assertThat(savedOrder.getTotalPrice())
                .isEqualTo(24_000);

        // 주문 상세 확인
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        OrderItem savedOrderItem =
                savedOrder.getOrderItems().get(0);

        assertThat(savedOrderItem.getMenuId()).isEqualTo(menuId);
        assertThat(savedOrderItem.getMenuName()).isEqualTo("불고기 피자");
        assertThat(savedOrderItem.getMenuPrice()).isEqualTo(menuPrice);
        assertThat(savedOrderItem.getQuantity()).isEqualTo(quantity);
        assertThat(savedOrderItem.getSubtotalPrice()).isEqualTo(24_000);

        // Order.addOrderItem()에서 양방향 연관관계가 연결됐는지 확인
        assertThat(savedOrderItem.getOrder())
                .isSameAs(savedOrder);

    }

    // 수량만 다르게 주문 요청을 만들기 위한 테스트 헬퍼 메서드
    private OrderCreateRequest createOrderRequest(int quantity) {
        return new OrderCreateRequest(
                storeId,
                "서울시 강남구",
                List.of(
                        new OrderItemCreateRequest(
                                menuId,
                                quantity
                        )
                )
        );
    }


}