package com.delivery.domain.menu.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.delivery.common.util.CryptoConverter;
import com.delivery.common.util.SsnEncryptor;
import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.global.config.CustomAuditorAware;
import com.delivery.global.config.EncryptConfig;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.AuditorAware;

// @DataJpaTest는 User/Address 엔티티가 쓰는 CryptoConverter(전화번호/주소 암호화)도
// 같은 영속성 유닛 메타모델에 포함시키려 하는데, 이 컨버터가 필요로 하는
// SsnEncryptor/AesBytesEncryptor 빈은 슬라이스에 기본으로 안 실려서 명시적으로 가져와야 함
// (Menu 도메인과 무관한 의존성이지만, 같은 EntityManagerFactory를 쓰기 때문에 필요함).
@DataJpaTest
@Import({
    JpaAuditingConfig.class,
    MenuRepositoryIntegrationTest.TestAuditorConfig.class,
    EncryptConfig.class,
    SsnEncryptor.class,
    CryptoConverter.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MenuRepositoryIntegrationTest extends AbstractIntegrationTest {

    // @Import(CustomAuditorAware.class)만으로는 이 테스트 슬라이스에서
    // "customAuditorAware"라는 빈 이름으로 등록되지 않아 @EnableJpaAuditing이 못 찾음 -
    // @Bean(name=...)으로 명시적으로 등록해야 함.
    @TestConfiguration
    static class TestAuditorConfig {
        @Bean(name = "customAuditorAware")
        public AuditorAware<String> customAuditorAware() {
            return new CustomAuditorAware();
        }
    }

    private static final UUID STORE_ID = UUID.randomUUID();

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
            MenuEntity deletedMenu =
                    menuRepository.save(new MenuEntity(STORE_ID, "메뉴3", null, 3000));
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
    @DisplayName("주문 가능 메뉴 조회")
    class FindByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse {

        @Test
        @DisplayName("존재·미삭제·미숨김·가게 소속이면 조회된다")
        void findsOrderableMenu() {
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = menuRepository.save(new MenuEntity(storeId, "김치찌개", "설명", 8000));

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                            menu.getMenuId(), storeId);

            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("다른 가게 소속이면 조회되지 않는다")
        void excludesMenuOfDifferentStore() {
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = menuRepository.save(new MenuEntity(storeId, "김치찌개", "설명", 8000));

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                            menu.getMenuId(), UUID.randomUUID());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("숨김 처리된 메뉴는 조회되지 않는다")
        void excludesHiddenMenu() {
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = menuRepository.save(new MenuEntity(storeId, "김치찌개", "설명", 8000));
            menu.updateHidden(true);
            menuRepository.saveAndFlush(menu);

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                            menu.getMenuId(), storeId);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("삭제된 메뉴는 조회되지 않는다")
        void excludesDeletedMenu() {
            UUID storeId = UUID.randomUUID();
            MenuEntity menu = menuRepository.save(new MenuEntity(storeId, "김치찌개", "설명", 8000));
            menu.delete("owner1");
            menuRepository.saveAndFlush(menu);

            Optional<MenuEntity> found =
                    menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNullAndHiddenIsFalse(
                            menu.getMenuId(), storeId);

            assertThat(found).isEmpty();
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
