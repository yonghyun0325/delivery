package com.delivery.domain.store.service;

import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.dto.response.RegionResponse;
import com.delivery.domain.store.entity.Region;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.RegionRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        if (regionRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new StoreException(StoreErrorCode.DUPLICATE_REGION);
        }

        Region region =
                Region.builder()
                        .name(request.name())
                        .latitude(request.latitude())
                        .longitude(request.longitude())
                        .build();

        return RegionResponse.from(regionRepository.save(region));
    }

    public List<RegionResponse> getRegions() {
        return regionRepository.findAllByDeletedAtIsNull().stream()
                .map(RegionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegionResponse updateRegion(UUID regionId, RegionRequest request) {
        Region region =
                regionRepository
                        .findByRegionIdAndDeletedAtIsNull(regionId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.REGION_NOT_FOUND));

        region.update(request.name(), request.latitude(), request.longitude());
        return RegionResponse.from(region);
    }

    @Transactional
    public void deleteRegion(UUID regionId, String deletedBy) {
        Region region =
                regionRepository
                        .findByRegionIdAndDeletedAtIsNull(regionId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.REGION_NOT_FOUND));

        region.delete(deletedBy);
    }
}
