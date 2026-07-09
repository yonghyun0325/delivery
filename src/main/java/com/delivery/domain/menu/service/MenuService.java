package com.delivery.domain.menu.service;

import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.domain.ai.service.AiService;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final AiService aiService;
    private final TransactionTemplate transactionTemplate;

    // 메뉴 생성
    // 외부 Gemini API 호출(aiService)이 DB 트랜잭션/커넥션을 물고 있지 않도록,
    // 이 메서드 자체는 트랜잭션 밖에서 실행되게 강제하고(NOT_SUPPORTED로 클래스 레벨
    // readOnly 트랜잭션을 덮어씀), 실제 DB 저장만 TransactionTemplate으로 짧게 묶는다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MenuResponse createMenu(
            UUID storeId,
            String name,
            String description,
            int price,
            boolean aiGeneration,
            String aiPrompt) {
        validateMenu(name, price);

        String finalDescription = description;
        if (aiGeneration) {
            if (aiPrompt == null || aiPrompt.isBlank()) {
                throw new AiException(AiErrorCode.AI_PROMPT_REQUIRED);
            }
            finalDescription = aiService.generateProductDescription(aiPrompt);
        }

        return saveMenu(storeId, name, finalDescription, price);
    }

    private MenuResponse saveMenu(UUID storeId, String name, String description, int price) {
        return transactionTemplate.execute(
                status -> {
                    MenuEntity menu = new MenuEntity(storeId, name, description, price);
                    return MenuResponse.from(menuRepository.save(menu));
                });
    }

    // 메뉴 목록 조회
    // 클래스 레벨 @Transactional(readOnly = true) 적용
    // 읽기 전용 -> 더티체킹/flush 생략. 조회 성능 높임.
    public List<MenuResponse> getStoreMenus(UUID storeId) {
        return menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(MenuResponse::from)
                .toList();
    }

    // 메뉴 단건 조회
    // 클래스 레벨 @Transactional(readOnly = true) 적용
    // 읽기 전용 -> 더티체킹/flush 생략. 조회 성능 높임.
    public MenuResponse getMenu(UUID menuId) {
        return MenuResponse.from(findMenu(menuId));
    }

    // 메뉴 수정
    @Transactional
    public MenuResponse updateMenu(UUID menuId, String name, String description, int price) {
        validateMenu(name, price);

        MenuEntity menu = findMenu(menuId);
        menu.update(name, description, price);
        return MenuResponse.from(menu);
    }

    // 숨김 상태 업데이트
    @Transactional
    public MenuResponse updateVisibility(UUID menuId, boolean hidden) {
        MenuEntity menu = findMenu(menuId);
        menu.updateHidden(hidden);
        return MenuResponse.from(menu);
    }

    // 메뉴 삭제 (Soft Delete)
    @Transactional
    public void deleteMenu(UUID menuId, String deletedBy) {
        MenuEntity menu = findMenu(menuId);
        menu.delete(deletedBy);
    }

    // 주문/장바구니 도메인에 제공하는 계약 - 존재·미삭제·미숨김·가게 소속을 한 번에 검증 후 스냅샷 반환.
    // 실패 사유(없음/다른 가게 소속/숨김/삭제됨)를 구분하지 않고 전부 MENU_NOT_FOUND로 응답 -
    // 존재 여부 자체를 노출하지 않기 위함(다른 404 응답들과 동일 원칙).
    public MenuSnapshot getOrderableMenu(UUID menuId, UUID storeId) {
        return MenuSnapshot.from(
                menuRepository
                        .findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(menuId, storeId)
                        .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND)));
    }

    // 수정/삭제 등 엔티티 자체가 필요한 내부 호출용 - 공개 API(getMenu)는 MenuResponse를 반환하므로 분리함
    private MenuEntity findMenu(UUID menuId) {
        return menuRepository
                .findByMenuIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));
    }

    // 컨트롤러를 거치지 않는 호출(내부 서비스 간 호출 등)에서도 규칙이 지켜지도록 서비스 레벨에서도 검증
    private void validateMenu(String name, int price) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new MenuException(MenuErrorCode.INVALID_MENU_NAME);
        }
        if (price <= 0) {
            throw new MenuException(MenuErrorCode.INVALID_MENU_PRICE);
        }
    }
}
