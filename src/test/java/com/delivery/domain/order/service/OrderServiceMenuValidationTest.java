package com.delivery.domain.order.service;

import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.request.OrderItemCreateRequest;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.order.exception.OrderErrorCode;
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
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID storeId;
    private UUID menuId;
    private Long currentUserId;

    private Store store;
    private MenuEntity menu;

    private OrderCreateRequest request;

    // given
    @BeforeEach
    void setUp() {
        // 테스트에서 공통으로 사용할 식별자
        storeId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        currentUserId = 1L;

        // Store와 MenuEntity는 생성자 접근이 제한돼 있으므로 Mock 객체로 사용
        store = mock(Store.class);
        menu = mock(MenuEntity.class);

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
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                orderService.createOrder(request, currentUserId)
        )
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.MENU_NOT_FOUND.getMessage());

        // 메뉴 검증에서 실패했으므로 주문은 저장되지 않아야 함
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 다른 가게에 속한 메뉴일 때
    @Test
    @DisplayName("주문 생성 시 메뉴가 다른 가게 소속이면 MENU_STORE_MISMATCH 예외가 발생한다")
    void createOrder_menuStoreMismatch() {

        // given
        UUID otherStoreId = UUID.randomUUID();

        when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                .thenReturn(Optional.of(menu));

        // 조회된 메뉴는 요청한 가게가 아닌 다른 가게 소속
        when(menu.getStoreId()).thenReturn(otherStoreId);

        // when & then
        assertThatThrownBy(() ->
                orderService.createOrder(request, currentUserId)
        )
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.MENU_STORE_MISMATCH.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 숨김 메뉴 주문시
    @Test
    @DisplayName("주문 생성 시 숨김 메뉴이면 MENU_NOT_AVAILABLE 예외가 발생한다")
    void createOrder_hiddenMenu() {

        // given
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                .thenReturn(Optional.of(menu));

        // 요청한 가게의 메뉴이지만 숨김 처리된 상태
        when(menu.getStoreId()).thenReturn(storeId);
        when(menu.isHidden()).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                orderService.createOrder(request, currentUserId)
        )
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.MENU_NOT_AVAILABLE.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 가격이 0 이하인 메뉴
    @Test
    @DisplayName("주문 생성 시 메뉴 가격이 0 이하이면 INVALID_MENU_PRICE 예외가 발생한다")
    void createOrder_invalidMenuPrice() {

        // given
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                .thenReturn(Optional.of(menu));

        when(menu.getStoreId()).thenReturn(storeId);

        when(menu.isHidden()).thenReturn(false);

        when(menu.getPrice()).thenReturn(0);

        // when & then
        assertThatThrownBy(() ->
                orderService.createOrder(request, currentUserId)
        )
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.INVALID_MENU_PRICE.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 정상 메뉴 주문 생성
    @Test
    @DisplayName("정상 메뉴이면 DB의 메뉴명과 가격으로 주문이 생성된다")
    void createOrder_success() {

        // given
        int quantity = 2;
        int menuPrice = 12_000;

        // 정상 케이스는 수량 2개로 별도 요청 생성
        OrderCreateRequest successRequest = createOrderRequest(quantity);

        when(store.getMinOrderAmount()).thenReturn(10_000);

        when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

        when(menu.getMenuId()).thenReturn(menuId);
        when(menu.getStoreId()).thenReturn(storeId);
        when(menu.isHidden()).thenReturn(false);
        when(menu.getName()).thenReturn("불고기 피자");
        when(menu.getPrice()).thenReturn(menuPrice);

        // save()로 전달된 Order 객체를 그대로 반환
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        orderService.createOrder(successRequest, currentUserId);

        // then
        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

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
