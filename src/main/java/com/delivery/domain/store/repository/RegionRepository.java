package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);
    boolean existsByNameAndDeletedAtIsNullAndRegionIdNot(String name, UUID regionId);
    List<Region> findAllByDeletedAtIsNull();
    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
}