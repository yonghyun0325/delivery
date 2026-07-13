package com.delivery.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import com.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock private MenuRepository menuRepository;

    @Mock private StoreRepository storeRepository;

    @Mock private AiService aiService;

    @Mock private TransactionTemplate transactionTemplate;

    @InjectMocks private MenuService menuService;

    // TransactionTemplate은 실제로는 콜백을 실행하고 그 결과를 반환하므로,
    // 목에서도 동일하게 동작하도록 스텁 - 아니면 항상 null이 반환됨.
    private void stubTransactionTemplateToRunCallback() {
        given(transactionTemplate.execute(any()))
                .willAnswer(
                        invocation -> {
                            TransactionCallback<?> callback = invocation.getArgument(0);
                            return callback.doInTransaction(null);
                        });
    }

    // STORE_ID 가게가 OWNER_ID 소유라고 스텁
    private void stubStoreOwnedBy(Long ownerId) {
        Store store = Store.builder().storeId(STORE_ID).userId(ownerId).build();
        given(storeRepository.findByStoreIdAndDeletedAtIsNull(STORE_ID))
                .willReturn(Optional.of(store));
    }

    @Nested
    @DisplayName("메뉴 생성")
    class CreateMenu {

        @Test
        @DisplayName("리포지토리에 저장하고 저장된 엔티티 기반의 응답을 반환한다")
        void createMenu_savesAndReturns() {

            stubTransactionTemplateToRunCallback();
            stubStoreOwnedBy(OWNER_ID);
            MenuEntity saved = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.save(any(MenuEntity.class))).willReturn(saved);

            MenuResponse result =
                    menuService.createMenu(
                            STORE_ID, "김치찌개", "설명", 8000, false, null, OWNER_ID, false);

            assertThat(result).isEqualTo(MenuResponse.from(saved));
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("aiGeneration이 true면 AI가 생성한 설명으로 메뉴를 생성한다")
        void createMenu_withAiGeneration_usesGeneratedDescription() {

            stubTransactionTemplateToRunCallback();
            stubStoreOwnedBy(OWNER_ID);
            given(aiService.generateProductDescription("김치찌개 설명 써줘")).willReturn("AI가 만든 설명");
            given(menuRepository.save(any(MenuEntity.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            MenuResponse result =
                    menuService.createMenu(
                            STORE_ID, "김치찌개", null, 8000, true, "김치찌개 설명 써줘", OWNER_ID, false);

            assertThat(result.description()).isEqualTo("AI가 만든 설명");
        }

        @Test
        @DisplayName("aiGeneration이 true인데 aiPrompt가 비어있으면 AI_PROMPT_REQUIRED 예외를 던진다")
        void createMenu_withAiGenerationButNoPrompt_throws() {

            stubStoreOwnedBy(OWNER_ID);

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(
                            () ->
                                    menuService.createMenu(
                                            STORE_ID, "김치찌개", null, 8000, true, " ", OWNER_ID,
                                            false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_PROMPT_REQUIRED);

            verifyNoInteractions(aiService);
        }

        @Test
        @DisplayName("가게가 존재하지 않으면 MENU_STORE_NOT_FOUND 예외를 던진다")
        void createMenu_throws_whenStoreNotFound() {

            given(storeRepository.findByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(
                            () ->
                                    menuService.createMenu(
                                            STORE_ID, "김치찌개", "설명", 8000, false, null, OWNER_ID,
                                            false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.MENU_STORE_NOT_FOUND);

            verifyNoInteractions(aiService, menuRepository, transactionTemplate);
        }

        @Test
        @DisplayName("가게 소유자가 아니면 NOT_MENU_STORE_OWNER 예외를 던진다")
        void createMenu_throws_whenNotStoreOwner() {

            stubStoreOwnedBy(OWNER_ID);

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(
                            () ->
                                    menuService.createMenu(
                                            STORE_ID,
                                            "김치찌개",
                                            "설명",
                                            8000,
                                            false,
                                            null,
                                            OTHER_USER_ID,
                                            false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.NOT_MENU_STORE_OWNER);
        }

        @Test
        @DisplayName("MANAGER/MASTER는 가게 소유자가 아니어도 생성할 수 있다(우회)")
        void createMenu_bypassesOwnership_forElevatedRole() {

            stubTransactionTemplateToRunCallback();
            stubStoreOwnedBy(OWNER_ID);
            MenuEntity saved = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);
            given(menuRepository.save(any(MenuEntity.class))).willReturn(saved);

            MenuResponse result =
                    menuService.createMenu(
                            STORE_ID, "김치찌개", "설명", 8000, false, null, OTHER_USER_ID, true);

            assertThat(result).isEqualTo(MenuResponse.from(saved));
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetMenu {

        @Test
        @DisplayName("가게 소유자가 조회하면 숨김 메뉴도 전체 필드로 반환한다")
        void getMenu_returnsMenuResponse_whenOwner() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);
            menu.updateHidden(true);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            MenuView result = menuService.getMenu(menuId, OWNER_ID, false);

            assertThat(result).isEqualTo(MenuResponse.from(menu));
        }

        @Test
        @DisplayName("손님이 숨김 아닌 메뉴를 조회하면 공개 필드로 반환한다")
        void getMenu_returnsPublicResponse_whenNotOwnerAndNotHidden() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            MenuView result = menuService.getMenu(menuId, OTHER_USER_ID, false);

            assertThat(result).isEqualTo(PublicMenuResponse.from(menu));
        }

        @Test
        @DisplayName("손님이 숨김 메뉴를 조회하면 MENU_NOT_FOUND 예외를 던진다")
        void getMenu_throws_whenNotOwnerAndHidden() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);
            menu.updateHidden(true);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(() -> menuService.getMenu(menuId, OTHER_USER_ID, false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("MANAGER/MASTER는 소유자가 아니어도 숨김 메뉴를 전체 필드로 조회한다")
        void getMenu_returnsMenuResponse_whenElevatedRole() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);
            menu.updateHidden(true);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));

            MenuView result = menuService.getMenu(menuId, OTHER_USER_ID, true);

            assertThat(result).isEqualTo(MenuResponse.from(menu));
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND 예외를 던진다")
        void getMenu_throws_whenNotFound() {

            UUID menuId = UUID.randomUUID();

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(() -> menuService.getMenu(menuId, OWNER_ID, false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 목록 조회")
    class GetStoreMenus {

        @Test
        @DisplayName("가게 소유자가 조회하면 숨김 메뉴를 포함해 전체 필드로 반환한다")
        void getStoreMenus_returnsMenuResponses_whenOwner() {

            MenuEntity visible = new MenuEntity(STORE_ID, "메뉴1", null, 1000);
            MenuEntity hidden = new MenuEntity(STORE_ID, "메뉴2", null, 2000);
            hidden.updateHidden(true);

            stubStoreOwnedBy(OWNER_ID);
            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(List.of(visible, hidden));

            List<MenuView> result = menuService.getStoreMenus(STORE_ID, OWNER_ID, false);

            assertThat(result)
                    .containsExactly(MenuResponse.from(visible), MenuResponse.from(hidden));
        }

        @Test
        @DisplayName("손님이 조회하면 숨김 메뉴를 뺀 공개 필드 목록만 반환한다")
        void getStoreMenus_returnsPublicResponses_whenNotOwner() {

            MenuEntity visible = new MenuEntity(STORE_ID, "메뉴1", null, 1000);

            stubStoreOwnedBy(OWNER_ID);
            given(menuRepository.findAllByStoreIdAndDeletedAtIsNullAndHiddenIsFalse(STORE_ID))
                    .willReturn(List.of(visible));

            List<MenuView> result = menuService.getStoreMenus(STORE_ID, OTHER_USER_ID, false);

            assertThat(result).containsExactly(PublicMenuResponse.from(visible));
        }

        @Test
        @DisplayName("MANAGER/MASTER는 소유자가 아니어도 숨김 메뉴를 포함해 전체 필드로 반환한다")
        void getStoreMenus_returnsMenuResponses_whenElevatedRole() {

            MenuEntity hidden = new MenuEntity(STORE_ID, "메뉴2", null, 2000);
            hidden.updateHidden(true);

            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(List.of(hidden));

            List<MenuView> result = menuService.getStoreMenus(STORE_ID, OTHER_USER_ID, true);

            assertThat(result).containsExactly(MenuResponse.from(hidden));
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class UpdateMenu {

        @Test
        @DisplayName("조회된 엔티티의 필드를 변경한다")
        void updateMenu_changesFields() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            MenuResponse result =
                    menuService.updateMenu(menuId, "된장찌개", "새 설명", 9000, OWNER_ID, false);

            assertThat(result.name()).isEqualTo("된장찌개");
            assertThat(result.description()).isEqualTo("새 설명");
            assertThat(result.price()).isEqualTo(9000);
        }

        @Test
        @DisplayName("가게 소유자가 아니면 NOT_MENU_STORE_OWNER 예외를 던진다")
        void updateMenu_throws_whenNotStoreOwner() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(
                            () ->
                                    menuService.updateMenu(
                                            menuId, "된장찌개", "새 설명", 9000, OTHER_USER_ID, false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.NOT_MENU_STORE_OWNER);
        }
    }

    @Nested
    @DisplayName("숨김 상태 변경")
    class UpdateVisibility {

        @Test
        @DisplayName("조회된 엔티티의 hidden 값을 변경한다")
        void updateVisibility_changesHidden() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            MenuResponse result = menuService.updateVisibility(menuId, true, OWNER_ID, false);

            assertThat(result.hidden()).isTrue();
        }

        @Test
        @DisplayName("MANAGER/MASTER는 가게 소유자가 아니어도 변경할 수 있다(우회)")
        void updateVisibility_bypassesOwnership_forElevatedRole() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            MenuResponse result = menuService.updateVisibility(menuId, true, OTHER_USER_ID, true);

            assertThat(result.hidden()).isTrue();
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class DeleteMenu {

        @Test
        @DisplayName("조회된 엔티티를 soft delete 처리한다")
        void deleteMenu_marksAsDeleted() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            menuService.deleteMenu(menuId, "owner1", OWNER_ID, false);

            assertThat(menu.isDeleted()).isTrue();
            assertThat(menu.getDeletedBy()).isEqualTo("owner1");
        }

        @Test
        @DisplayName("가게 소유자가 아니면 NOT_MENU_STORE_OWNER 예외를 던지고 삭제되지 않는다")
        void deleteMenu_throws_whenNotStoreOwner() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));
            stubStoreOwnedBy(OWNER_ID);

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(() -> menuService.deleteMenu(menuId, "other", OTHER_USER_ID, false))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.NOT_MENU_STORE_OWNER);

            assertThat(menu.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 일괄 삭제")
    class DeleteMenusByStoreId {

        @Test
        @DisplayName("가게에 속한 삭제되지 않은 메뉴를 전부 soft delete 처리한다")
        void deleteMenusByStoreId_marksAllAsDeleted() {

            MenuEntity menu1 = new MenuEntity(STORE_ID, "메뉴1", null, 1000);
            MenuEntity menu2 = new MenuEntity(STORE_ID, "메뉴2", null, 2000);

            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(List.of(menu1, menu2));

            menuService.deleteMenusByStoreId(STORE_ID, "SYSTEM");

            assertThat(menu1.isDeleted()).isTrue();
            assertThat(menu1.getDeletedBy()).isEqualTo("SYSTEM");
            assertThat(menu2.isDeleted()).isTrue();
            assertThat(menu2.getDeletedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("삭제할 메뉴가 없으면 아무 일도 일어나지 않는다")
        void deleteMenusByStoreId_doesNothing_whenNoMenus() {

            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(List.of());

            menuService.deleteMenusByStoreId(STORE_ID, "SYSTEM");
            // 예외 없이 끝나면 성공 - 별도 assert 불필요
        }
    }

    @Nested
    @DisplayName("주문 가능 메뉴 조회")
    class GetOrderableMenu {

        @Test
        @DisplayName("존재·미삭제·미숨김·가게 소속이면 스냅샷을 반환한다")
        void getOrderableMenu_returnsSnapshot_whenValid() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(
                            menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                                    menuId, STORE_ID))
                    .willReturn(Optional.of(menu));

            MenuSnapshot result = menuService.getOrderableMenu(menuId, STORE_ID);

            assertThat(result).isEqualTo(MenuSnapshot.from(menu));
        }

        @Test
        @DisplayName("조건을 만족하지 않으면(없음/다른 가게/숨김/삭제) MENU_NOT_FOUND 예외를 던진다")
        void getOrderableMenu_throws_whenNotOrderable() {

            UUID menuId = UUID.randomUUID();

            given(
                            menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                                    menuId, STORE_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(() -> menuService.getOrderableMenu(menuId, STORE_ID))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.MENU_NOT_FOUND);
        }
    }
}
