package com.delivery.domain.menu.dto.response;

import com.delivery.domain.menu.entity.MenuEntity;
import java.time.LocalDateTime;
import java.util.UUID;

// 가게 소유자/관리자용 검색 응답 - hidden/updatedAt 등 운영 메타데이터 포함.
// 손님에게는 대신 PublicMenuSearchResponse가 나간다(MenuSearchView 참고).
public record MenuSearchResponse(
        UUID menuId,
        UUID storeId,
        String storeName,
        String name,
        String description,
        int price,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt)
        implements MenuSearchView {

    public static MenuSearchResponse from(MenuEntity menu, String storeName) {
        return new MenuSearchResponse(
                menu.getMenuId(),
                menu.getStoreId(),
                storeName,
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                menu.isHidden(),
                menu.getCreatedAt(),
                menu.getUpdatedAt());
    }
}
