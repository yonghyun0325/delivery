package com.delivery.domain.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartItem;
import com.delivery.domain.cart.repository.CartItemRepository;
import com.delivery.domain.cart.repository.CartRepository;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.user.entity.Role;
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

    @InjectMocks private CartService cartService;

    @Nested
    @DisplayName("Get My Cart")
    class GetMyCart {

        @Test
        @DisplayName("returns empty cart when no active cart exists")
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
    @DisplayName("Add Cart Item")
    class AddCartItem {

        @Test
        @DisplayName("creates a new cart and first item")
        void addCartItem_creates_cart_when_cart_does_not_exist() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Jjajangmyeon", "basic", 7000);
            Cart cart = createCart(1L, storeId);
            CartItem cartItem = CartItem.create(cart, menuId, "Jjajangmyeon", 2, 7000L);

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
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
        }

        @Test
        @DisplayName("adds quantity when the same menu already exists")
        void addCartItem_increases_quantity_when_same_menu_exists() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Jjamppong", "spicy", 9000);
            Cart cart = createCart(1L, storeId);
            CartItem cartItem = CartItem.create(cart, menuId, "Jjamppong", 1, 9000L);

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
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
        @DisplayName("creates a new active row when only a soft-deleted item exists")
        void addCartItem_creates_new_item_when_previous_item_is_soft_deleted() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(storeId, "Bibimbap", "basic", 10000);
            Cart cart = createCart(1L, storeId);
            CartItem newItem = CartItem.create(cart, menuId, "Bibimbap", 1, 10000L);

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(newItem);
            when(cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart))
                    .thenReturn(List.of(newItem));

            CartResponse response = cartService.addCartItem(userDetails, menuId, 1);

            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).menuName()).isEqualTo("Bibimbap");
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("rejects a menu from another store")
        void addCartItem_fails_when_store_is_different() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(UUID.randomUUID(), "Fried Rice", "basic", 8000);
            Cart cart = createCart(1L, UUID.randomUUID());

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("rejects a hidden menu")
        void addCartItem_fails_when_menu_is_hidden() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(UUID.randomUUID(), "Hidden Menu", "private", 5000);
            menu.updateHidden(true);

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(MenuException.class);
        }

        @Test
        @DisplayName("rejects a deleted menu")
        void addCartItem_fails_when_menu_is_deleted() {
            CustomUserDetails userDetails = createUserDetails(1L);
            UUID menuId = UUID.randomUUID();

            when(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addCartItem(userDetails, menuId, 1))
                    .isInstanceOf(MenuException.class);
        }
    }

    @Nested
    @DisplayName("Update Cart Item")
    class UpdateCartItem {

        @Test
        @DisplayName("updates quantity for owned cart item")
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
        @DisplayName("rejects update for another user's cart item")
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
    @DisplayName("Delete Cart Item")
    class DeleteCartItem {

        @Test
        @DisplayName("rejects delete for another user's cart item")
        void deleteCartItem_fails_when_cart_item_owned_by_another_user() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(2L, UUID.randomUUID());
            CartItem cartItem = CartItem.create(cart, UUID.randomUUID(), "Menu", 1, 5000L);

            when(cartItemRepository.findByCartItemIdAndDeletedAtIsNull(cartItem.getCartItemId()))
                    .thenReturn(Optional.of(cartItem));

            assertThatThrownBy(
                            () ->
                                    cartService.deleteCartItem(
                                            userDetails, cartItem.getCartItemId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("soft deletes only the item when more items remain")
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
        @DisplayName("soft deletes the cart when the last item is removed")
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
    @DisplayName("Clear Cart")
    class ClearCart {

        @Test
        @DisplayName("soft deletes all items and the cart")
        void clearMyCart_success() {
            CustomUserDetails userDetails = createUserDetails(1L);
            Cart cart = createCart(1L, UUID.randomUUID());
            CartItem first = CartItem.create(cart, UUID.randomUUID(), "A", 1, 5000L);
            CartItem second = CartItem.create(cart, UUID.randomUUID(), "B", 2, 6000L);

            when(cartRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartAndDeletedAtIsNull(cart))
                    .thenReturn(List.of(first, second));

            cartService.clearMyCart(userDetails);

            assertThat(first.isDeleted()).isTrue();
            assertThat(second.isDeleted()).isTrue();
            assertThat(cart.isDeleted()).isTrue();
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
}
