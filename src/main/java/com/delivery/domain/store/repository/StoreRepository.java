package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

    @Query("SELECT s FROM Store s WHERE s.deletedAt IS NULL " +
            "AND (:categoryId IS NULL OR s.categoryId = :categoryId) " +
            "AND (:regionId IS NULL OR s.regionId = :regionId) " +
            "AND (:name IS NULL OR s.name LIKE %:name%)")
    Page<Store> findStores(@Param("categoryId") UUID categoryId,
                           @Param("regionId") UUID regionId,
                           @Param("name") String name,
                           Pageable pageable);

    //가게 중복 등록
    boolean existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(Long userId, String name, UUID regionId);
}