package com.delivery.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
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

@ExtendWith(MockitoExtension.class)
class MenuServiceV1Test {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Mock private MenuRepository menuRepository;

    @InjectMocks private MenuServiceV1 menuService;

    @Nested
    @DisplayName("메뉴 생성")
    class CreateMenu {

        @Test
        @DisplayName("리포지토리에 저장하고 저장된 엔티티를 반환한다")
        void createMenu_savesAndReturns() {

            MenuEntity saved = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.save(any(MenuEntity.class))).willReturn(saved);

            MenuEntity result = menuService.createMenu(STORE_ID, "김치찌개", "설명", 8000);

            assertThat(result).isEqualTo(saved);
            verify(menuRepository).save(any(MenuEntity.class));
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetMenu {

        @Test
        @DisplayName("존재하면 엔티티를 반환한다")
        void getMenu_returnsEntity_whenExists() {

            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.of(menu));

            MenuEntity result = menuService.getMenu(menuId);

            assertThat(result).isEqualTo(menu);
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND 예외를 던진다")
        void getMenu_throws_whenNotFound() {

            UUID menuId = UUID.randomUUID();

            given(menuRepository.findByMenuIdAndDeletedAtIsNull(menuId))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(BusinessException.class)
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

            List<MenuEntity> menus = List.of(new MenuEntity(STORE_ID, "메뉴1", null, 1000));

            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID)).willReturn(menus);

            List<MenuEntity> result = menuService.getStoreMenus(STORE_ID);

            assertThat(result).isEqualTo(menus);
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

            MenuEntity result = menuService.updateMenu(menuId, "된장찌개", "새 설명", 9000);

            assertThat(result.getName()).isEqualTo("된장찌개");
            assertThat(result.getDescription()).isEqualTo("새 설명");
            assertThat(result.getPrice()).isEqualTo(9000);
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

            MenuEntity result = menuService.updateVisibility(menuId, true);

            assertThat(result.isHidden()).isTrue();
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
}
