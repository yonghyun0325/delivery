package com.delivery.domain.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartItem;
import com.delivery.domain.cart.exception.CartErrorCode;
import com.delivery.domain.cart.exception.CartException;
import com.delivery.domain.cart.repository.CartItemRepository;
import com.delivery.domain.cart.repository.CartRepository;
import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private MenuService menuService;
    @Mock private UserRepository userRepository;

    @InjectMocks private CartService cartService;

    @Nested
    @DisplayName("내 장바구니 조회")
    class GetMyCart {

        @Test
        @DisplayName("활성 장바구니가 없으면 빈 장바구니를 반환한다")
        void getMyCart_returns_empty_when_cart_does_not_exist() {
            CustomUserDetails userDetails = createUserDetails(1L);

            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            CartResponse response = cartService.getMyCart(userDetails);

            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.items()).isEmpty();
            assertThat(response.totalPrice()).isZero();
        }
    }

    @Nested
    @DisplayName("장바구니 항목 추가")
    class AddCartItem {

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니와 첫 항목을 생성한다")
        void addCartItem_creates_cart_when_cart_does_not_exist() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Jjajangmyeon", "basic", 7000);
            Cart cart = createCart(1L, storeId);
            CartItem cartItem = CartItem.create(cart, menuId, "Jjajangmyeon", 2, 7000L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .thenReturn(Optional.of(menu));
            when(menuService.getOrderableMenu(menuId, storeId))
                    .thenReturn(new MenuSnapshot(menuId, "Jjajangmyeon", 7000));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartItemRepository.findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(cartItem));

            CartResponse response = cartService.addCartItem(userDetails, menuId, 2);

            assertThat(response.storeId()).isEqualTo(storeId);
            assertThat(response.totalQuantity()).isEqualTo(2);
            assertThat(response.totalPrice()).isEqualTo(14000L);
            assertThat(response.items().get(0).menuName()).isEqualTo("Jjajangmyeon");
            assertThat(response.items().get(0).subtotalPrice()).isEqualTo(14000L);
            verify(userRepository).findByIdForUpdate(1L);
        }

        @Test
        @DisplayName("같은 메뉴를 다시 담으면 기존 수량에 합산한다")
        void addCartItem_increases_quantity_when_same_menu_exists() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Jjamppong", "spicy", 9000);
            Cart cart = createCart(1L, storeId);
            CartItem cartItem = CartItem.create(cart, menuId, "Jjamppong", 1, 9000L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
            when(menuService.getOrderableMenu(menuId, storeId))
                    .thenReturn(new MenuSnapshot(menuId, "Jjamppong", 9000));
            when(cartItemRepository.findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(cartItem));

            CartResponse response = cartService.addCartItem(userDetails, menuId, 2);

            assertThat(cartItem.getQuantity()).isEqualTo(3);
            assertThat(response.totalQuantity()).isEqualTo(3);
            assertThat(response.totalPrice()).isEqualTo(27000L);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("소프트 삭제된 같은 메뉴만 있으면 새 활성 행을 만든다")
        void addCartItem_creates_new_item_when_previous_item_is_soft_deleted() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Bibimbap", "rice", 10000);
            Cart cart = createCart(1L, storeId);
            CartItem newItem = CartItem.create(cart, menuId, "Bibimbap", 1, 10000L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
            when(menuService.getOrderableMenu(menuId, storeId))
                    .thenReturn(new MenuSnapshot(menuId, "Bibimbap", 10000));
            when(cartItemRepository.findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(newItem);
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(newItem));

            CartResponse response = cartService.addCartItem(userDetails, menuId, 1);

            assertThat(response.items()).hasSize(1);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("다른 가게 메뉴는 추가할 수 없다")
        void addCartItem_fails_when_store_is_different() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID cartStoreId = UUID.randomUUID();
            UUID otherStoreId = UUID.randomUUID();
            Cart cart = createCart(1L, cartStoreId);
            MenuEntity menu = new MenuEntity(otherStoreId, "Other Store Menu", "desc", 8000);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(CartException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_STORE_MISMATCH);

            verify(menuService, never()).getOrderableMenu(any(), any());
        }

        @Test
        @DisplayName("숨김 메뉴는 추가할 수 없다")
        void addCartItem_fails_when_menu_is_hidden() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Hidden Menu", "private", 5000);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .thenReturn(Optional.of(menu));
            when(menuService.getOrderableMenu(menuId, storeId))
                    .thenThrow(new MenuException(MenuErrorCode.MENU_NOT_FOUND));

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(MenuException.class);
        }

        @Test
        @DisplayName("삭제된 메뉴는 추가할 수 없다")
        void addCartItem_fails_when_menu_is_deleted() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(MenuException.class);
        }

        @Test
        @DisplayName("메뉴 주문 가능 계약을 사용한다")
        void addCartItem_uses_menu_service_orderable_contract() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Tteokbokki", "spicy", 6000);
            Cart cart = createCart(1L, storeId);
            CartItem cartItem = CartItem.create(cart, menuId, "Tteokbokki", 1, 6000L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .thenReturn(Optional.of(menu));
            when(menuService.getOrderableMenu(menuId, storeId))
                    .thenReturn(new MenuSnapshot(menuId, "Tteokbokki", 6000));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartItemRepository.findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(cartItem));

            cartService.addCartItem(userDetails, menuId, 1);

            verify(menuService).getOrderableMenu(eq(menuId), eq(storeId));
        }
    }

    @Nested
    @DisplayName("장바구니 항목 수정")
    class UpdateCartItem {

        @Test
        @DisplayName("본인 장바구니 항목 수량을 수정할 수 있다")
        void updateCartItem_success_when_owned_by_user() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(1L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(cartItem));

            CartResponse response =
                    cartService.updateCartItem(userDetails, cartItem.getCartItemId(), 3);

            assertThat(cartItem.getQuantity()).isEqualTo(3);
            assertThat(response.totalPrice()).isEqualTo(15000L);
        }

        @Test
        @DisplayName("다른 사용자 장바구니 항목은 수정할 수 없다")
        void updateCartItem_fails_when_cart_item_owned_by_another_user() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(2L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));

            assertThatThrownBy(
                            () ->
                                    cartService.updateCartItem(
                                            userDetails, cartItem.getCartItemId(), 3))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("장바구니 항목 삭제")
    class DeleteCartItem {

        @Test
        @DisplayName("다른 사용자 장바구니 항목은 삭제할 수 없다")
        void deleteCartItem_fails_when_cart_item_owned_by_another_user() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(2L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));

            assertThatThrownBy(
                            () -> cartService.deleteCartItem(userDetails, cartItem.getCartItemId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("다른 항목이 남아 있으면 항목만 소프트 삭제한다")
        void deleteCartItem_deletes_only_item_when_cart_still_has_items() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(1L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.countByCartAndDeletedAtIsNull(cart)).thenReturn(1L);

            cartService.deleteCartItem(userDetails, cartItem.getCartItemId());

            assertThat(cartItem.isDeleted()).isTrue();
            assertThat(cart.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("마지막 항목을 삭제하면 장바구니도 소프트 삭제한다")
        void deleteCartItem_deletes_cart_when_last_item_removed() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(1L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.countByCartAndDeletedAtIsNull(cart)).thenReturn(0L);

            cartService.deleteCartItem(userDetails, cartItem.getCartItemId());

            assertThat(cartItem.isDeleted()).isTrue();
            assertThat(cart.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("장바구니 비우기")
    class ClearCart {

        @Test
        @DisplayName("항목과 장바구니를 모두 소프트 삭제한다")
        void clearMyCart_success() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(1L, UUID.randomUUID());
            CartItem first = CartItem.create(cart, UUID.randomUUID(), "A", 1, 5000L);
            CartItem second = CartItem.create(cart, UUID.randomUUID(), "B", 2, 6000L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartAndDeletedAtIsNull(cart))
                    .thenReturn(List.of(first, second));

            cartService.clearMyCart(userDetails);

            assertThat(first.isDeleted()).isTrue();
            assertThat(second.isDeleted()).isTrue();
            assertThat(cart.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("장바구니가 없어도 비우기 요청은 멱등하게 처리한다")
        void clearMyCart_is_idempotent_when_cart_does_not_exist() {
            CustomUserDetails userDetails = createUserDetails(1L);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(createUser()));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            cartService.clearMyCart(userDetails);

            verify(cartItemRepository, never()).findAllByCartAndDeletedAtIsNull(any(Cart.class));
        }
    }

    private CustomUserDetails createUserDetails(Long userId) {
        return CustomUserDetails.builder()
                .id(userId)
                .username("customer")
                .authorities(Set.of(new SimpleGrantedAuthority(Role.CUSTOMER.getAuthority())))
                .build();
    }

    private Cart createCart(Long userId, UUID storeId) {
        return Cart.create(userId, storeId);
    }

    private User createUser() {
        return User.builder()
                .username("customer")
                .password("password")
                .nickName("nick")
                .phoneNumber("01012345678")
                .build();
    }
}
