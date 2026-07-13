package com.delivery.domain.menu.repository;

import com.delivery.domain.menu.entity.MenuEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    Optional<MenuEntity> findByMenuIdAndDeletedAtIsNull(UUID menuId);

    List<MenuEntity> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    List<MenuEntity> findAllByStoreIdAndDeletedAtIsNullAndHiddenIsFalse(UUID storeId);

    Optional<MenuEntity> findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
            UUID menuId, UUID storeId);
}
