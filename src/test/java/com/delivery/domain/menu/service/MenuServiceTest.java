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
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.repository.MenuRepository;
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

    @Mock private MenuRepository menuRepository;

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

    @Nested
    @DisplayName("메뉴 생성")
    class CreateMenu {

        @Test
        @DisplayName("리포지토리에 저장하고 저장된 엔티티 기반의 응답을 반환한다")
        void createMenu_savesAndReturns() {

            stubTransactionTemplateToRunCallback();
            MenuEntity saved = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.save(any(MenuEntity.class))).willReturn(saved);

            MenuResponse result = menuService.createMenu(STORE_ID, "김치찌개", "설명", 8000, false, null);

            assertThat(result).isEqualTo(MenuResponse.from(saved));
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("aiGeneration이 true면 AI가 생성한 설명으로 메뉴를 생성한다")
        void createMenu_withAiGeneration_usesGeneratedDescription() {

            stubTransactionTemplateToRunCallback();
            given(aiService.generateProductDescription("김치찌개 설명 써줘")).willReturn("AI가 만든 설명");
            given(menuRepository.save(any(MenuEntity.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            MenuResponse result =
                    menuService.createMenu(STORE_ID, "김치찌개", null, 8000, true, "김치찌개 설명 써줘");

            assertThat(result.description()).isEqualTo("AI가 만든 설명");
        }

        @Test
        @DisplayName("aiGeneration이 true인데 aiPrompt가 비어있으면 AI_PROMPT_REQUIRED 예외를 던진다")
        void createMenu_withAiGenerationButNoPrompt_throws() {

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(
                            () -> menuService.createMenu(STORE_ID, "김치찌개", null, 8000, true, " "))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_PROMPT_REQUIRED);

            verifyNoInteractions(aiService);
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetMenu {

        @Test
        @DisplayName("존재하면 응답을 반환한다")
        void getMenu_returnsResponse_whenExists() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));

            MenuResponse result = menuService.getMenu(menuId);

            assertThat(result).isEqualTo(MenuResponse.from(menu));
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND 예외를 던진다")
        void getMenu_throws_whenNotFound() {

            UUID menuId = UUID.randomUUID();

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(MenuException.class)
                    .isThrownBy(() -> menuService.getMenu(menuId))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 목록 조회")
    class GetStoreMenus {

        @Test
        @DisplayName("삭제되지 않은 메뉴 목록을 반환한다")
        void getStoreMenus_returnsList() {

            MenuEntity menu = new MenuEntity(STORE_ID, "메뉴1", null, 1000);
            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID))
                    .willReturn(List.of(menu));

            List<MenuResponse> result = menuService.getStoreMenus(STORE_ID);

            assertThat(result).containsExactly(MenuResponse.from(menu));
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

            MenuResponse result = menuService.updateMenu(menuId, "된장찌개", "새 설명", 9000);

            assertThat(result.name()).isEqualTo("된장찌개");
            assertThat(result.description()).isEqualTo("새 설명");
            assertThat(result.price()).isEqualTo(9000);
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

            MenuResponse result = menuService.updateVisibility(menuId, true);

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

            menuService.deleteMenu(menuId, "owner1");

            assertThat(menu.isDeleted()).isTrue();
            assertThat(menu.getDeletedBy()).isEqualTo("owner1");
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
