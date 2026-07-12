package com.delivery.domain.menu.dto.response;

import com.delivery.domain.menu.entity.MenuEntity;
import java.util.UUID;

// 주문/장바구니 도메인이 주문 전환 시점에 참조하는 스냅샷 - 검증 통과 후의 최소 정보만 노출
public record MenuSnapshot(UUID menuId, String name, int price) {

    public static MenuSnapshot from(MenuEntity menu) {
        return new MenuSnapshot(menu.getMenuId(), menu.getName(), menu.getPrice());
    }
}
