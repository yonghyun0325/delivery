package com.delivery.domain.cart.service;

import com.delivery.domain.cart.dto.response.CartItemResponse;
import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartItem;
import com.delivery.domain.cart.exception.CartErrorCode;
import com.delivery.domain.cart.exception.CartException;
import com.delivery.domain.cart.repository.CartItemRepository;
import com.delivery.domain.cart.repository.CartRepository;
import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final MenuService menuService;
    private final UserRepository userRepository;

    public CartResponse getMyCart(CustomUserDetails userDetails) {
        return cartRepository
                .findByUserIdAndDeletedAtIsNull(userDetails.getId())
                .map(this::toCartResponse)
                .orElseGet(() -> CartResponse.empty(userDetails.getId()));
    }

    @Transactional
    public CartResponse addCartItem(CustomUserDetails userDetails, UUID menuId, int quantity) {
        lockUser(userDetails.getId());
        Cart cart = cartRepository.findByUserIdAndDeletedAtIsNull(userDetails.getId()).orElse(null);
        UUID menuStoreId = getMenuStoreId(menuId);

        if (cart != null && !cart.getStoreId().equals(menuStoreId)) {
            throw new CartException(CartErrorCode.CART_STORE_MISMATCH);
        }

        UUID storeId = cart != null ? cart.getStoreId() : menuStoreId;
        MenuSnapshot menuSnapshot = menuService.getOrderableMenu(menuId, storeId);

        if (cart == null) {
            cart = cartRepository.save(Cart.create(userDetails.getId(), storeId));
        }

        Cart activeCart = cart;

        CartItem cartItem =
                cartItemRepository
                        .findByCartAndMenuIdAndDeletedAtIsNull(activeCart, menuId)
                        .map(
                                existingItem -> {
                                    existingItem.addQuantity(quantity);
                                    return existingItem;
                                })
                        .orElseGet(
                                () ->
                                        cartItemRepository.save(
                                                CartItem.create(
                                                        activeCart,
                                                        menuId,
                                                        menuSnapshot.name(),
                                                        quantity,
                                                        menuSnapshot.price())));

        return toCartResponse(cartItem.getCart());
    }

    @Transactional
    public CartResponse updateCartItem(
            CustomUserDetails userDetails, UUID cartItemId, int quantity) {
        CartItem cartItem = getOwnedCartItem(userDetails.getId(), cartItemId);
        cartItem.updateQuantity(quantity);
        return toCartResponse(cartItem.getCart());
    }

    @Transactional
    public void deleteCartItem(CustomUserDetails userDetails, UUID cartItemId) {
        CartItem cartItem = getOwnedCartItem(userDetails.getId(), cartItemId);
        Cart cart = cartItem.getCart();
        String deletedBy = userDetails.getId() + "_" + userDetails.getUsername();

        cartItem.delete(deletedBy);

        if (cartItemRepository.countByCartAndDeletedAtIsNull(cart) == 0) {
            cart.delete(deletedBy);
        }
    }

    @Transactional
    public void clearMyCart(CustomUserDetails userDetails) {
        lockUser(userDetails.getId());
        Cart cart =
                cartRepository
                        .findByUserIdAndDeletedAtIsNull(userDetails.getId())
                        .orElse(null);

        if (cart == null) {
            return;
        }

        String deletedBy = userDetails.getId() + "_" + userDetails.getUsername();

        for (CartItem cartItem : cartItemRepository.findAllByCartAndDeletedAtIsNull(cart)) {
            cartItem.delete(deletedBy);
        }

        cart.delete(deletedBy);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items =
                cartItemRepository.findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(cart).stream()
                        .map(CartItemResponse::from)
                        .toList();
        return CartResponse.from(cart, items);
    }

    private CartItem getOwnedCartItem(Long userId, UUID cartItemId) {
        CartItem cartItem =
                cartItemRepository
                        .findByCartItemIdAndDeletedAtIsNull(cartItemId)
                        .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));

        if (cartItem.getCart().isDeleted()) {
            throw new BusinessException(GlobalErrorCode.NOT_FOUND);
        }

        if (!cartItem.getCart().isOwnedBy(userId)) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN);
        }

        return cartItem;
    }

    private UUID getMenuStoreId(UUID menuId) {
        return menuRepository
                .findByMenuIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND))
                .getStoreId();
    }

    private void lockUser(Long userId) {
        userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));
    }
}
