package com.delivery.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.request.OrderItemCreateRequest;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.order.exception.OrderException;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.exception.PaymentErrorCode;
import com.delivery.domain.payment.exception.PaymentException;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceMenuValidationTest {

    @Mock private OrderRepository orderRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private MenuService menuService;
    @Mock private PaymentService paymentService;

    @InjectMocks private OrderService orderService;

    private UUID storeId;
    private UUID menuId;
    private Long currentUserId;
    private Store store;
    private OrderCreateRequest request;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        currentUserId = 1L;
        store = mockStore();
        request = createOrderRequest(1);

        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(Optional.of(store));
        when(store.getIsOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("주문 생성 중 메뉴가 없으면 MENU_NOT_FOUND 예외가 발생한다")
    void createOrder_menuNotFound() {
        when(menuService.getOrderableMenu(menuId, storeId))
                .thenThrow(new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        assertThatThrownBy(() -> orderService.createOrder(request, currentUserId))
                .isInstanceOf(MenuException.class)
                .hasMessage(MenuErrorCode.MENU_NOT_FOUND.getMessage());

        verify(menuService).getOrderableMenu(menuId, storeId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never())
                .createPayment(any(UUID.class), any(Long.class), any(Integer.class), any(PaymentMethod.class));
    }

    @Test
    @DisplayName("주문 가능한 메뉴면 주문을 생성하고 결제 생성 계약도 호출한다")
    void createOrder_success() {
        int quantity = 2;
        int menuPrice = 12_000;
        OrderCreateRequest successRequest = createOrderRequest(quantity);

        when(store.getMinOrderAmount()).thenReturn(10_000);
        when(menuService.getOrderableMenu(menuId, storeId))
                .thenReturn(new MenuSnapshot(menuId, "Burger", menuPrice));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(
                        invocation -> {
                            Order order = invocation.getArgument(0);
                            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
                            return order;
                        });

        orderService.createOrder(successRequest, currentUserId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(menuService).getOrderableMenu(menuId, storeId);

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUserId()).isEqualTo(currentUserId);
        assertThat(savedOrder.getStoreId()).isEqualTo(storeId);
        assertThat(savedOrder.getDeliveryAddress()).isEqualTo("서울시 강남구");
        assertThat(savedOrder.getTotalPrice()).isEqualTo(24_000);
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        OrderItem savedOrderItem = savedOrder.getOrderItems().get(0);
        assertThat(savedOrderItem.getMenuId()).isEqualTo(menuId);
        assertThat(savedOrderItem.getMenuName()).isEqualTo("Burger");
        assertThat(savedOrderItem.getMenuPrice()).isEqualTo(menuPrice);
        assertThat(savedOrderItem.getQuantity()).isEqualTo(quantity);
        assertThat(savedOrderItem.getSubtotalPrice()).isEqualTo(24_000);
        assertThat(savedOrderItem.getOrder()).isSameAs(savedOrder);

        verify(paymentService)
                .createPayment(
                        savedOrder.getId(),
                        currentUserId,
                        savedOrder.getTotalPrice(),
                        PaymentMethod.CARD);
    }

    @Test
    @DisplayName("결제 생성이 실패하면 주문 생성도 예외를 전파한다")
    void createOrder_fail_when_payment_creation_fails() {
        when(store.getMinOrderAmount()).thenReturn(10_000);
        when(menuService.getOrderableMenu(menuId, storeId))
                .thenReturn(new MenuSnapshot(menuId, "Burger", 12_000));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(
                        invocation -> {
                            Order order = invocation.getArgument(0);
                            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
                            return order;
                        });
        doThrow(new PaymentException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS))
                .when(paymentService)
                .createPayment(any(UUID.class), eq(currentUserId), eq(12_000), eq(PaymentMethod.CARD));

        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(1), currentUserId))
                .isInstanceOf(PaymentException.class)
                .hasMessage(PaymentErrorCode.PAYMENT_ALREADY_EXISTS.getMessage());
    }

    private OrderCreateRequest createOrderRequest(int quantity) {
        return new OrderCreateRequest(
                storeId,
                "서울시 강남구",
                List.of(new OrderItemCreateRequest(menuId, quantity)));
    }

    private Store mockStore() {
        return org.mockito.Mockito.mock(Store.class);
    }
}
