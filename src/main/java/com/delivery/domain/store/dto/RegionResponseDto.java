package com.delivery.domain.store.dto;

import com.delivery.domain.store.entity.Region;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionResponseDto {

    private UUID regionId;
    private String name;
    private Double latitude;
    private Double longitude;

    public static RegionResponseDto from(Region region) {
        return RegionResponseDto.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .latitude(region.getLatitude())
                .longitude(region.getLongitude())
                .build();
    }
}
