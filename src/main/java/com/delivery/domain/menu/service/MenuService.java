package com.delivery.domain.menu.service;

import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.domain.ai.service.AiService;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.dto.response.MenuView;
import com.delivery.domain.menu.dto.response.PublicMenuResponse;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
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
    private final StoreRepository storeRepository;
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
            String aiPrompt,
            Long requesterId,
            boolean bypassOwnership) {
        // AI 호출(비용/시간이 드는 작업) 전에 먼저 검증해 불필요한 Gemini 호출을 막음
        validateStoreOwnership(storeId, requesterId, bypassOwnership);
        validateMenu(name, price);

        // AI 생성은 DB 저장(saveMenu)보다 먼저, 트랜잭션 밖에서 끝까지 실행됨.
        // generateProductDescription이 예외를 던지면 그대로 위로 전파되어 saveMenu 자체가
        // 호출되지 않으므로("AI 실패 시 메뉴 등록 전체 실패" 정책), 트랜잭션이 롤백되는 게
        // 아니라 애초에 트랜잭션이 열릴 기회조차 없다.
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
    // 역할별 응답 분기(3차): 그 가게 소유자이거나 MANAGER/MASTER면 숨김 메뉴도 포함해서
    // 전체 필드(MenuResponse)로, 아니면 숨김 메뉴는 빼고 공개 필드(PublicMenuResponse)로.
    public List<MenuView> getStoreMenus(UUID storeId, Long requesterId, boolean isElevatedRole) {
        boolean canViewHidden = canViewHiddenMenus(storeId, requesterId, isElevatedRole);

        List<MenuEntity> menus =
                canViewHidden
                        ? menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId)
                        : menuRepository.findAllByStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                                storeId);

        return menus.stream().map(menu -> (MenuView) toMenuView(menu, canViewHidden)).toList();
    }

    // 메뉴 단건 조회
    // 클래스 레벨 @Transactional(readOnly = true) 적용
    // 읽기 전용 -> 더티체킹/flush 생략. 조회 성능 높임.
    // 숨김 메뉴를 볼 권한이 없는 조회자에게는 403이 아니라 404(MENU_NOT_FOUND)로 응답 -
    // 다른 404 응답들과 동일하게 존재 여부 자체를 노출하지 않기 위함.
    public MenuView getMenu(UUID menuId, Long requesterId, boolean isElevatedRole) {
        MenuEntity menu = findMenu(menuId);
        boolean canViewHidden = canViewHiddenMenus(menu.getStoreId(), requesterId, isElevatedRole);

        if (menu.isHidden() && !canViewHidden) {
            throw new MenuException(MenuErrorCode.MENU_NOT_FOUND);
        }

        return toMenuView(menu, canViewHidden);
    }

    private MenuView toMenuView(MenuEntity menu, boolean canViewHidden) {
        return canViewHidden ? MenuResponse.from(menu) : PublicMenuResponse.from(menu);
    }

    // MANAGER/MASTER는 항상 가능, 그 외(OWNER 포함)는 해당 가게의 실제 소유자일 때만 가능.
    // 가게가 존재하지 않으면(연결이 끊긴 storeId 등) 안전하게 false로 처리.
    private boolean canViewHiddenMenus(UUID storeId, Long requesterId, boolean isElevatedRole) {
        if (isElevatedRole) {
            return true;
        }
        return storeRepository
                .findByStoreIdAndDeletedAtIsNull(storeId)
                .map(store -> store.getUserId().equals(requesterId))
                .orElse(false);
    }

    // 메뉴 수정
    @Transactional
    public MenuResponse updateMenu(
            UUID menuId,
            String name,
            String description,
            int price,
            Long requesterId,
            boolean bypassOwnership) {
        validateMenu(name, price);

        MenuEntity menu = findMenu(menuId);
        validateStoreOwnership(menu.getStoreId(), requesterId, bypassOwnership);
        menu.update(name, description, price);
        return MenuResponse.from(menu);
    }

    // 숨김 상태 업데이트
    @Transactional
    public MenuResponse updateVisibility(
            UUID menuId, boolean hidden, Long requesterId, boolean bypassOwnership) {
        MenuEntity menu = findMenu(menuId);
        validateStoreOwnership(menu.getStoreId(), requesterId, bypassOwnership);
        menu.updateHidden(hidden);
        return MenuResponse.from(menu);
    }

    // 메뉴 삭제 (Soft Delete)
    @Transactional
    public void deleteMenu(
            UUID menuId, String deletedBy, Long requesterId, boolean bypassOwnership) {
        MenuEntity menu = findMenu(menuId);
        validateStoreOwnership(menu.getStoreId(), requesterId, bypassOwnership);
        menu.delete(deletedBy);
    }

    // 가게 삭제 시 그 가게에 속한 메뉴를 일괄 소프트 삭제 - 실제 삭제(DELETE)가 아니라
    // deleted_at/deleted_by만 채움. 소유권 검증은 이미 가게를 삭제한 호출자(Store 도메인)
    // 책임이라 여기서 다시 하지 않음 - 조회한 엔티티는 영속 상태라 JPA 더티체킹으로
    // 트랜잭션 커밋 시 자동 반영되므로 save() 호출 불필요.
    @Transactional
    public void deleteMenusByStoreId(UUID storeId, String deletedBy) {
        menuRepository
                .findAllByStoreIdAndDeletedAtIsNull(storeId)
                .forEach(menu -> menu.delete(deletedBy));
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

    // Store 소유권(2차) 검증 - StoreService를 거치지 않고 StoreRepository를 직접 조회함.
    // StoreService 자신도 CategoryRepository/RegionRepository를 직접 조회하는 방식이라
    // 같은 관례를 따름. bypassOwnership=true(MANAGER/MASTER)면 소유자 일치 여부는 스킵하되,
    // 가게 자체가 존재하는지는 항상 확인함.
    private void validateStoreOwnership(UUID storeId, Long requesterId, boolean bypassOwnership) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_STORE_NOT_FOUND));

        if (!bypassOwnership && !store.getUserId().equals(requesterId)) {
            throw new MenuException(MenuErrorCode.NOT_MENU_STORE_OWNER);
        }
    }
}
