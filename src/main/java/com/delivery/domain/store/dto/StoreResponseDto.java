package com.delivery.domain.store.dto;

import com.delivery.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StoreResponseDto {

    private UUID storeId;
    private Long userId;
    private UUID categoryId;
    private UUID regionId;
    private String name;
    private String address;
    private String phone;
    private String description;
    private Integer minOrderAmount;
    private Boolean isOpen;
    private Double averageRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
                .storeId(store.getStoreId())
                .userId(store.getUserId())
                .categoryId(store.getCategoryId())
                .regionId(store.getRegionId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .description(store.getDescription())
                .minOrderAmount(store.getMinOrderAmount())
                .isOpen(store.getIsOpen())
                .averageRating(store.getAverageRating())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}