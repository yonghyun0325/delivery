package com.delivery.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.fixture.StoreTestFixture;
import com.delivery.domain.menu.repository.MenuRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.store.service.StoreService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// Store→Menu 캐스케이드 삭제 계약을 실제 DB로 검증한다. StoreServiceUnitTest는
// MenuService를 목으로 막고, MenuServiceTest도 MenuRepository를 목으로 막아서
// "가게를 삭제하면 진짜로 그 가게의 메뉴가 소프트 삭제되는지"는 양쪽 단위 테스트
// 어디에서도 실제로 증명된 적이 없다 - 이 세션 초반 PR #62 머지 과정에서 바로 이
// 캐스케이드 호출 자체가 조용히 사라졌던 회귀를 직접 잡았던 지점이기도 하다.
// @Transactional로 테스트가 만든 Store/Menu 데이터를 종료 시 롤백 - 공유 Testcontainers
// DB에 다른 도메인 테스트와 데이터가 남아 충돌하는 것을 방지한다.
@SpringBootTest(
        properties = {
            "gemini.api-key=test-dummy-key",
            "gemini.base-url=https://generativelanguage.googleapis.com",
            "gemini.model=gemini-1.5-flash"
        })
@Transactional
class MenuStoreCascadeIntegrationTest extends AbstractIntegrationTest {

    @Autowired private StoreService storeService;
    @Autowired private StoreRepository storeRepository;
    @Autowired private MenuRepository menuRepository;

    @Test
    @DisplayName("가게 삭제 시 소속 메뉴가 실제로 소프트 삭제되고 deletedBy가 일치한다")
    void deleteStore_cascadesToMenus_withMatchingDeletedBy() {
        Long ownerId = 800001L;
        Store store = storeRepository.save(StoreTestFixture.DEFAULT.createStore(ownerId));
        UUID storeId = store.getStoreId();
        MenuEntity menu1 = menuRepository.save(new MenuEntity(storeId, "메뉴1", null, 1000));
        MenuEntity menu2 = menuRepository.save(new MenuEntity(storeId, "메뉴2", null, 2000));

        String deletedBy = ownerId + "_owner1";
        storeService.deleteStore(storeId, ownerId, false, deletedBy);

        // 삭제되지 않은 메뉴로는 더 이상 조회되지 않아야 한다.
        List<MenuEntity> remaining = menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);
        assertThat(remaining).isEmpty();

        // 실제로 소프트 삭제됐고, 가게 삭제 시 넘긴 deletedBy와 정확히 같은 값이 찍혀야 한다
        // (StoreService가 자기 자신의 deletedBy와 다른 값을 Menu 쪽에 잘못 넘기는 회귀 방지).
        MenuEntity deletedMenu1 = menuRepository.findById(menu1.getMenuId()).orElseThrow();
        MenuEntity deletedMenu2 = menuRepository.findById(menu2.getMenuId()).orElseThrow();
        assertThat(deletedMenu1.isDeleted()).isTrue();
        assertThat(deletedMenu1.getDeletedBy()).isEqualTo(deletedBy);
        assertThat(deletedMenu2.isDeleted()).isTrue();
        assertThat(deletedMenu2.getDeletedBy()).isEqualTo(deletedBy);

        // 가게 자신도 같은 deletedBy로 소프트 삭제됐는지 확인
        Store deletedStore = storeRepository.findById(storeId).orElseThrow();
        assertThat(deletedStore.isDeleted()).isTrue();
        assertThat(deletedStore.getDeletedBy()).isEqualTo(deletedBy);
    }

    @Test
    @DisplayName("본인 소유가 아닌 가게를 OWNER가 삭제하려 하면 메뉴는 그대로 남는다")
    void deleteStore_doesNotCascade_whenNotOwner() {
        Long realOwnerId = 800002L;
        Long otherOwnerId = 800003L;
        Store store = storeRepository.save(StoreTestFixture.DEFAULT.createStore(realOwnerId));
        UUID storeId = store.getStoreId();
        menuRepository.save(new MenuEntity(storeId, "메뉴1", null, 1000));

        assertThatThrownBy(
                        () -> storeService.deleteStore(storeId, otherOwnerId, false, "other_owner"))
                .isInstanceOf(StoreException.class);

        assertThat(menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId)).hasSize(1);
    }
}
