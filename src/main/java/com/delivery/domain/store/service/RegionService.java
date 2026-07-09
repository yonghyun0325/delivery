package com.delivery.domain.store.service;

import com.delivery.domain.store.dto.RegionRequestDto;
import com.delivery.domain.store.dto.RegionResponseDto;
import com.delivery.domain.store.entity.Region;
import com.delivery.domain.store.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    @Transactional
    public RegionResponseDto createRegion(RegionRequestDto request) {
        if (regionRepository.existsByName(request.getName())) {
            throw new RuntimeException("이미 등록된 지역입니다.");
        }

        Region region = Region.builder()
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        return RegionResponseDto.from(regionRepository.save(region));
    }

    @Transactional(readOnly = true)
    public List<RegionResponseDto> getRegions() {
        return regionRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(RegionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegionResponseDto updateRegion(UUID regionId, RegionRequestDto request) {
        Region region = regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)
                .orElseThrow(() -> new RuntimeException("지역을 찾을 수 없습니다."));

        region.update(request.getName(), request.getLatitude(), request.getLongitude());
        return RegionResponseDto.from(region);
    }

    @Transactional
    public void deleteRegion(UUID regionId, String deletedBy) {
        Region region = regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)
                .orElseThrow(() -> new RuntimeException("지역을 찾을 수 없습니다."));

        region.delete(deletedBy);
    }
}