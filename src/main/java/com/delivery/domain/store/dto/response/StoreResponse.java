package com.delivery.domain.store.dto.response;

import com.delivery.domain.store.entity.Store;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreResponse(
        UUID storeId,
        UUID categoryId,
        UUID regionId,
        String name,
        String address,
        String phone,
        String description,
        Integer minOrderAmount,
        Boolean isOpen,
        Double averageRating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getStoreId(),
                store.getCategoryId(),
                store.getRegionId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getDescription(),
                store.getMinOrderAmount(),
                store.getIsOpen(),
                store.getAverageRating(),
                store.getCreatedAt(),
                store.getUpdatedAt());
    }
}
