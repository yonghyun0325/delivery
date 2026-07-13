package com.delivery.domain.cart.repository;

import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findAllByCartAndDeletedAtIsNullOrderByCreatedAtAsc(Cart cart);

    List<CartItem> findAllByCartAndDeletedAtIsNull(Cart cart);

    Optional<CartItem> findByCartAndMenuIdAndDeletedAtIsNull(Cart cart, UUID menuId);

    Optional<CartItem> findByCartItemIdAndDeletedAtIsNull(UUID cartItemId);

    long countByCartAndDeletedAtIsNull(Cart cart);
}
