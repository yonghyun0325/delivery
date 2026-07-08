package com.delivery.domain.menu.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.global.config.JpaAuditingConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MenuRepositoryIntegrationTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired private MenuRepository menuRepository;

    @Nested
    @DisplayName("메뉴 단건 조회")
    class FindByMenuIdAndDeletedAtIsNull {

        @Test
        @DisplayName("삭제되지 않은 메뉴는 조회된다")
        void findsNonDeletedMenu() {
            MenuEntity menu = menuRepository.save(new MenuEntity(STORE_ID, "김치찌개", "설명", 8000));

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndDeletedAtIsNull(menu.getMenuId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("김치찌개");
        }

        @Test
        @DisplayName("삭제된 메뉴는 조회되지 않는다")
        void excludesDeletedMenu() {
            MenuEntity menu = menuRepository.save(new MenuEntity(STORE_ID, "김치찌개", "설명", 8000));
            menu.delete("owner1");
            menuRepository.saveAndFlush(menu);

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndDeletedAtIsNull(menu.getMenuId());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 목록 조회")
    class FindAllByStoreIdAndDeletedAtIsNull {

        @Test
        @DisplayName("같은 가게의 삭제되지 않은 메뉴만 반환한다")
        void returnsOnlyNonDeletedMenusOfStore() {
            menuRepository.save(new MenuEntity(STORE_ID, "메뉴1", null, 1000));
            menuRepository.save(new MenuEntity(STORE_ID, "메뉴2", null, 2000));
            MenuEntity deletedMenu = menuRepository.save(new MenuEntity(STORE_ID, "메뉴3", null, 3000));
            deletedMenu.delete("owner1");
            menuRepository.saveAndFlush(deletedMenu);
            menuRepository.save(new MenuEntity(UUID.randomUUID(), "다른가게메뉴", null, 4000));

            List<MenuEntity> result = menuRepository.findAllByStoreIdAndDeletedAtIsNull(STORE_ID);

            assertThat(result)
                    .extracting(MenuEntity::getName)
                    .containsExactlyInAnyOrder("메뉴1", "메뉴2");
        }
    }

    @Nested
    @DisplayName("price CHECK 제약조건")
    class PriceCheckConstraint {

        @Test
        @DisplayName("price가 0 이하면 DB 제약조건 위반으로 저장에 실패한다")
        void rejectsNonPositivePrice() {
            MenuEntity invalidMenu = new MenuEntity(STORE_ID, "무료메뉴", null, 0);

            assertThatThrownBy(() -> menuRepository.saveAndFlush(invalidMenu))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
