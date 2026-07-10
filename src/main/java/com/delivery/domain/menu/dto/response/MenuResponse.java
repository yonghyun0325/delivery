package com.delivery.domain.menu.dto.response;

import com.delivery.domain.menu.entity.MenuEntity;
import java.time.LocalDateTime;
import java.util.UUID;

// 가게 소유자/관리자용 전체 응답 - hidden/updatedAt 등 운영 메타데이터 포함.
// 손님에게는 대신 PublicMenuResponse가 나간다(MenuView 참고).
public record MenuResponse(
        UUID menuId,
        UUID storeId,
        String name,
        String description,
        int price,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt)
        implements MenuView {

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
