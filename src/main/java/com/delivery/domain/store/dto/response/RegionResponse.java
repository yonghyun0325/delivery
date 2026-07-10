package com.delivery.domain.store.dto.response;

import com.delivery.domain.store.entity.Region;
import java.util.UUID;

public record RegionResponse(
        UUID regionId,
        String name,
        Double latitude,
        Double longitude
) {
    public static RegionResponse from(Region region) {
        return new RegionResponse(
                region.getRegionId(),
                region.getName(),
                region.getLatitude(),
                region.getLongitude()
        );
    }
}