package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Store;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

    Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

    boolean existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(
            Long userId, String name, UUID regionId);

    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    boolean existsByRegionIdAndDeletedAtIsNull(UUID regionId);
}
