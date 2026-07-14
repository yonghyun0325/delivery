package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Region;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);

    List<Region> findAllByDeletedAtIsNull();

    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
}
