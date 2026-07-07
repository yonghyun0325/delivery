package com.delivery.domain.menu.service;

import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceV1 {

    private final MenuRepository menuRepository;

    // 메뉴 생성
    @Transactional
    public MenuEntity createMenu(UUID storeId, String name, String description, int price) {
        MenuEntity menu = new MenuEntity(storeId, name, description, price);
        return menuRepository.save(menu);
    }

    // 메뉴 목록 조회
    // 클래스 레벨 @Transactional(readOnly = true) 적용
    // 읽기 전용 -> 더티체킹/flush 생략. 조회 성능 높임.
    public List<MenuEntity> getStoreMenus(UUID storeId) {
        return menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);
    }

    // 메뉴 단건 조회
    // 클래스 레벨 @Transactional(readOnly = true) 적용
    // 읽기 전용 -> 더티체킹/flush 생략. 조회 성능 높임.
    public MenuEntity getMenu(UUID menuId) {
        return menuRepository
                .findByMenuIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.MENU_NOT_FOUND));
    }

    // 메뉴 수정
    @Transactional
    public MenuEntity updateMenu(UUID menuId, String name, String description, int price) {
        MenuEntity menu = getMenu(menuId);
        menu.update(name, description, price);
        return menu;
    }

    // 숨김 상태 업데이트
    @Transactional
    public MenuEntity updateVisibility(UUID menuId, boolean hidden) {
        MenuEntity menu = getMenu(menuId);
        menu.updateHidden(hidden);
        return menu;
    }

    // 메뉴 삭제 (Soft Delete)
    @Transactional
    public void deleteMenu(UUID menuId, String deletedBy) {
        MenuEntity menu = getMenu(menuId);
        menu.delete(deletedBy);
    }
}
