package com.delivery.domain.menu.dto.response;

import com.delivery.domain.menu.entity.MenuEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public record MenuResponse(
        UUID menuId,
        UUID storeId,
        String name,
        String description,
        int price,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MenuResponse from(MenuEntity menu) {
        return new MenuResponse(
                menu.getMenuId(),
                menu.getStoreId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                menu.isHidden(),
                menu.getCreatedAt(),
                menu.getUpdatedAt());
    }
}
