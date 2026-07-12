package com.delivery.domain.menu.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MenuEntityTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("생성 시 isHidden은 false다")
        void createMenu_setsDefaults() {
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            assertThat(menu.getStoreId()).isEqualTo(STORE_ID);
            assertThat(menu.getName()).isEqualTo("김치찌개");
            assertThat(menu.getDescription()).isEqualTo("설명");
            assertThat(menu.getPrice()).isEqualTo(8000);
            assertThat(menu.isHidden()).isFalse();
        }
    }

    @Nested
    @DisplayName("수정")
    class Update {

        @Test
        @DisplayName("update()는 name, description, price를 변경한다")
        void update_changesFields() {
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            menu.update("된장찌개", "새 설명", 9000);

            assertThat(menu.getName()).isEqualTo("된장찌개");
            assertThat(menu.getDescription()).isEqualTo("새 설명");
            assertThat(menu.getPrice()).isEqualTo(9000);
        }

        @Test
        @DisplayName("updateHidden()은 숨김 여부만 변경한다")
        void updateHidden_changesHiddenOnly() {
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            menu.updateHidden(true);

            assertThat(menu.isHidden()).isTrue();
            assertThat(menu.getName()).isEqualTo("김치찌개");
        }
    }

    @Nested
    @DisplayName("삭제")
    class Delete {

        @Test
        @DisplayName("delete() 호출 전에는 삭제되지 않은 상태다")
        void isDeleted_falseBeforeDelete() {
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            assertThat(menu.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("delete() 호출 시 deletedAt/deletedBy가 채워지고 isDeleted()는 true다")
        void delete_marksAsDeleted() {
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);

            menu.delete("owner1");

            assertThat(menu.isDeleted()).isTrue();
            assertThat(menu.getDeletedAt()).isNotNull();
            assertThat(menu.getDeletedBy()).isEqualTo("owner1");
        }
    }
}
