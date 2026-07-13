package com.delivery.domain.menu.dto.response;

import com.delivery.domain.menu.entity.MenuEntity;
import java.util.UUID;

// 손님(가게 소유자/관리자가 아닌 조회자)에게 노출하는 검색 응답.
// hidden/updatedAt 같은 운영 메타데이터는 빼고 화면에 보여줄 정보 + storeName만 담는다.
public record PublicMenuSearchResponse(
        UUID menuId, UUID storeId, String storeName, String name, String description, int price)
        implements MenuSearchView {

    public static PublicMenuSearchResponse from(MenuEntity menu, String storeName) {
        return new PublicMenuSearchResponse(
                menu.getMenuId(),
                menu.getStoreId(),
                storeName,
                menu.getName(),
                menu.getDescription(),
                menu.getPrice());
    }
}
