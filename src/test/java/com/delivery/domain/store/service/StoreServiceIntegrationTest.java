package com.delivery.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.entity.Category;
import com.delivery.domain.store.entity.Region;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import java.util.UUID;

import com.delivery.domain.user.UserDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "gemini.api-key=test-dummy-key",
        "gemini.base-url=https://generativelanguage.googleapis.com",
        "gemini.model=gemini-1.5-flash"
})
@Transactional
class StoreServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private StoreService storeService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private Category savedCategory;
    private Region savedRegion;
    private final Long OWNER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        savedCategory = categoryRepository.save(Category.builder().name("한식").build());
        savedRegion = regionRepository.save(
                Region.builder().name("강남구").latitude(37.5).longitude(127.0).build());
    }

    private StoreRequest defaultRequest() {
        return new StoreRequest(
                savedCategory.getCategoryId(),
                savedRegion.getRegionId(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트 가게입니다",
                10000);
    }

    private StoreResponse createDefaultStore() {
        return storeService.createStore(OWNER_ID, defaultRequest());
    }

    @Nested
    @DisplayName("가게 등록")
    class CreateStore {

        @Test
        @DisplayName("정상적으로 가게가 등록된다.")
        void createStore_success() {
            StoreResponse response = storeService.createStore(OWNER_ID, defaultRequest());

            assertThat(response.name()).isEqualTo("테스트 가게");
            assertThat(response.address()).isEqualTo("서울시 강남구");
            assertThat(response.isOpen()).isFalse();
            assertThat(response.averageRating()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("중복된 가게 등록 시 예외가 발생한다.")
        void createStore_fail_when_duplicate() {
            storeService.createStore(OWNER_ID, defaultRequest());

            assertThatThrownBy(() -> storeService.createStore(OWNER_ID, defaultRequest()))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("이미 등록된 가게입니다.");
        }
    }

    @Nested
    @DisplayName("가게 조회")
    class GetStore {

        @Test
        @DisplayName("가게 단건 조회에 성공한다.")
        void getStore_success() {
            StoreResponse created = createDefaultStore();

            StoreResponse found = storeService.getStore(created.storeId());

            assertThat(found.storeId()).isEqualTo(created.storeId());
            assertThat(found.name()).isEqualTo("테스트 가게");
        }

        @Test
        @DisplayName("존재하지 않는 가게 조회 시 예외가 발생한다.")
        void getStore_fail_when_not_found() {
            assertThatThrownBy(() -> storeService.getStore(UUID.randomUUID()))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("가게를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("가게 검색 (QueryDSL)")
    class SearchStores {

        @Test
        @DisplayName("필터 없이 전체 가게를 조회한다.")
        void searchStores_no_filter() {
            storeService.createStore(OWNER_ID, defaultRequest());
            storeService.createStore(OWNER_ID, new StoreRequest(
                    savedCategory.getCategoryId(), savedRegion.getRegionId(),
                    "다른 가게", "서울시 서초구", "01098765432", null, 5000));

            Page<StoreResponse> result = storeService.getStores(null, null, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("이름으로 가게를 검색한다.")
        void searchStores_by_name() {
            storeService.createStore(OWNER_ID, defaultRequest());
            storeService.createStore(OWNER_ID, new StoreRequest(
                    savedCategory.getCategoryId(), savedRegion.getRegionId(),
                    "다른 가게", "서울시 서초구", "01098765432", null, 5000));

            Page<StoreResponse> result = storeService.getStores(null, null, "테스트", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("테스트 가게");
        }

        @Test
        @DisplayName("카테고리로 가게를 필터링한다.")
        void searchStores_by_category() {
            Category otherCategory = categoryRepository.save(Category.builder().name("중식").build());

            storeService.createStore(OWNER_ID, defaultRequest());
            storeService.createStore(OWNER_ID, new StoreRequest(
                    otherCategory.getCategoryId(), savedRegion.getRegionId(),
                    "중식 가게", "서울시 서초구", "01098765432", null, 5000));

            Page<StoreResponse> result = storeService.getStores(
                    savedCategory.getCategoryId(), null, null, PageRequest.of(0, 10));

            assertThat(result.getContent()).allMatch(s -> s.categoryId().equals(savedCategory.getCategoryId()));
        }

        @Test
        @DisplayName("지역으로 가게를 필터링한다.")
        void searchStores_by_region() {
            Region otherRegion = regionRepository.save(Region.builder().name("부산").build());

            storeService.createStore(OWNER_ID, defaultRequest());
            storeService.createStore(OWNER_ID, new StoreRequest(
                    savedCategory.getCategoryId(), otherRegion.getRegionId(),
                    "부산 가게", "부산시 해운대구", "01011112222", null, 5000));

            Page<StoreResponse> result = storeService.getStores(
                    null, savedRegion.getRegionId(), null, PageRequest.of(0, 10));

            assertThat(result.getContent()).allMatch(s -> s.regionId().equals(savedRegion.getRegionId()));
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다.")
        void searchStores_no_result() {
            Page<StoreResponse> result = storeService.getStores(null, null, "없는가게이름xyz", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("회원 탈퇴 이벤트가 발생해도 가게는 삭제되지 않는다.")
        void store_persists_after_user_deletion() {
            storeService.createStore(OWNER_ID, defaultRequest());

            applicationEventPublisher.publishEvent(new UserDeletedEvent(OWNER_ID, "testuser"));

            Page<StoreResponse> result = storeService.getStores(null, null, null, PageRequest.of(0, 10));
            assertThat(result.getContent()).anyMatch(s -> s.name().equals("테스트 가게"));
        }
    }

    @Nested
    @DisplayName("가게 수정")
    class UpdateStore {

        @Test
        @DisplayName("소유자가 가게를 수정한다.")
        void updateStore_success_when_owner() {
            StoreResponse created = createDefaultStore();
            StoreRequest updateRequest = new StoreRequest(
                    savedCategory.getCategoryId(), savedRegion.getRegionId(),
                    "수정된 가게", "서울시 송파구", "01011112222", "수정됨", 20000);

            StoreResponse updated = storeService.updateStore(created.storeId(), OWNER_ID, false, updateRequest);

            assertThat(updated.name()).isEqualTo("수정된 가게");
            assertThat(updated.address()).isEqualTo("서울시 송파구");
            assertThat(updated.minOrderAmount()).isEqualTo(20000);
        }

        @Test
        @DisplayName("MANAGER/MASTER는 다른 사람의 가게도 수정할 수 있다.")
        void updateStore_success_when_elevated() {
            StoreResponse created = createDefaultStore();
            StoreRequest updateRequest = new StoreRequest(
                    savedCategory.getCategoryId(), savedRegion.getRegionId(),
                    "관리자 수정 가게", "서울시 마포구", "01033334444", null, 15000);

            StoreResponse updated = storeService.updateStore(created.storeId(), OTHER_USER_ID, true, updateRequest);

            assertThat(updated.name()).isEqualTo("관리자 수정 가게");
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 수정 시 예외가 발생한다.")
        void updateStore_fail_when_access_denied() {
            StoreResponse created = createDefaultStore();
            StoreRequest updateRequest = new StoreRequest(
                    savedCategory.getCategoryId(), savedRegion.getRegionId(),
                    "수정 시도", "서울시 강서구", "01055556666", null, 10000);

            assertThatThrownBy(() -> storeService.updateStore(created.storeId(), OTHER_USER_ID, false, updateRequest))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("해당 가게에 대한 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("영업 상태 변경")
    class UpdateStoreStatus {

        @Test
        @DisplayName("소유자가 영업 상태를 변경한다.")
        void updateStoreStatus_success() {
            StoreResponse created = createDefaultStore();
            assertThat(created.isOpen()).isFalse();

            StoreResponse updated = storeService.updateStoreStatus(created.storeId(), OWNER_ID, false, true);

            assertThat(updated.isOpen()).isTrue();
        }

        @Test
        @DisplayName("MANAGER/MASTER는 다른 사람의 가게 영업 상태도 변경할 수 있다.")
        void updateStoreStatus_success_when_elevated() {
            StoreResponse created = createDefaultStore();

            StoreResponse updated = storeService.updateStoreStatus(created.storeId(), OTHER_USER_ID, true, true);

            assertThat(updated.isOpen()).isTrue();
        }
    }

    @Nested
    @DisplayName("가게 삭제")
    class DeleteStore {

        @Test
        @DisplayName("소유자가 가게를 삭제하면 조회가 불가능하다.")
        void deleteStore_success_when_owner() {
            StoreResponse created = createDefaultStore();

            storeService.deleteStore(created.storeId(), OWNER_ID, false, OWNER_ID + "_owner");

            assertThatThrownBy(() -> storeService.getStore(created.storeId()))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("가게를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("MANAGER/MASTER는 다른 사람의 가게도 삭제할 수 있다.")
        void deleteStore_success_when_elevated() {
            StoreResponse created = createDefaultStore();

            storeService.deleteStore(created.storeId(), OTHER_USER_ID, true, OTHER_USER_ID + "_manager");

            assertThatThrownBy(() -> storeService.getStore(created.storeId()))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("가게를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 삭제 시 예외가 발생한다.")
        void deleteStore_fail_when_access_denied() {
            StoreResponse created = createDefaultStore();

            assertThatThrownBy(() -> storeService.deleteStore(created.storeId(), OTHER_USER_ID, false, OTHER_USER_ID + "_other"))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("해당 가게에 대한 권한이 없습니다.");
        }
    }
}