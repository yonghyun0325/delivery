package com.delivery.domain.menu.repository;

import com.delivery.domain.menu.entity.MenuEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    Optional<MenuEntity> findByMenuIdAndDeletedAtIsNull(UUID menuId);

    List<MenuEntity> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    List<MenuEntity> findAllByStoreIdAndDeletedAtIsNullAndHiddenIsFalse(UUID storeId);

    Optional<MenuEntity> findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
            UUID menuId, UUID storeId);

    // 횡단 검색(전체 가게 대상) - MANAGER/MASTER용, 숨김 메뉴 포함
    @Query(
            "SELECT m FROM MenuEntity m WHERE m.deletedAt IS NULL "
                    + "AND (:name IS NULL OR m.name LIKE %:name%)")
    Page<MenuEntity> searchAllMenus(@Param("name") String name, Pageable pageable);

    // 횡단 검색(전체 가게 대상) - 일반 조회자용, 숨김 메뉴 제외
    @Query(
            "SELECT m FROM MenuEntity m WHERE m.deletedAt IS NULL AND m.hidden = false "
                    + "AND (:name IS NULL OR m.name LIKE %:name%)")
    Page<MenuEntity> searchVisibleMenus(@Param("name") String name, Pageable pageable);
}
