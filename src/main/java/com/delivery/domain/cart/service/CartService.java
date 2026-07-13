package com.delivery.domain.cart.service;

import com.delivery.domain.cart.dto.response.CartItemResponse;
import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartItem;
import com.delivery.domain.cart.repository.CartItemRepository;
import com.delivery.domain.cart.repository.CartRepository;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
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

    public CartResponse getMyCart(CustomUserDetails userDetails) {
        return cartRepository
                .findByUserIdAndDeletedAtIsNull(userDetails.getId())
                .map(this::toCartResponse)
                .orElseGet(() -> CartResponse.empty(userDetails.getId()));
    }

    @Transactional
    public CartResponse addCartItem(CustomUserDetails userDetails, UUID menuId, int quantity) {
        MenuEntity menu = getOrderableMenu(menuId);
        Cart cart =
                cartRepository
                        .findByUserIdAndDeletedAtIsNull(userDetails.getId())
                        .orElseGet(
                                () ->
                                        cartRepository.save(
                                                Cart.create(userDetails.getId(), menu.getStoreId())));

        validateStoreConsistency(cart, menu.getStoreId());

        CartItem cartItem =
                cartItemRepository
                        .findByCartAndMenuIdAndDeletedAtIsNull(cart, menuId)
                        .map(
                                existingItem -> {
                                    existingItem.addQuantity(quantity);
                                    return existingItem;
                                })
                        .orElseGet(
                                () ->
                                        cartItemRepository.save(
                                                CartItem.create(
                                                        cart,
                                                        menuId,
                                                        menu.getName(),
                                                        quantity,
                                                        menu.getPrice())));

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
        Cart cart =
                cartRepository
                        .findByUserIdAndDeletedAtIsNull(userDetails.getId())
                        .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));

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

    private MenuEntity getOrderableMenu(UUID menuId) {
        MenuEntity menu =
                menuRepository
                        .findByMenuIdAndDeletedAtIsNull(menuId)
                        .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        if (menu.isHidden()) {
            throw new MenuException(MenuErrorCode.MENU_NOT_FOUND);
        }

        return menu;
    }

    private void validateStoreConsistency(Cart cart, UUID storeId) {
        if (!cart.getStoreId().equals(storeId)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }
    }
}
