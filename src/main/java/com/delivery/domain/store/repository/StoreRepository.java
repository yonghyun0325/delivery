package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

    Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

    boolean existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(Long userId, String name, UUID regionId);

    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    boolean existsByRegionIdAndDeletedAtIsNull(UUID regionId);
}