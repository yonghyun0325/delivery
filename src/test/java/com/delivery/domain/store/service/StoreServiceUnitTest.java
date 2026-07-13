package com.delivery.domain.store.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.store.exception.StoreException;
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
class StoreServiceUnitTest {
    @Mock private StoreRepository storeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private com.delivery.domain.menu.service.MenuService menuService;
    @InjectMocks private StoreService storeService;

    private StoreRequest createStoreRequest() {
        return new StoreRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트",
                10000);
    }

    @Nested
    @DisplayName("가게 등록 실패 테스트")
    class CreateStore {
        @Test
        @DisplayName("중복된 가게이면 예외가 발생해야 한다.")
        void createStore_fail_when_duplicate() {
            // given
            StoreRequest request = createStoreRequest();
            when(storeRepository.existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(
                    any(), any(), any())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> storeService.createStore(1L, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("이미 등록된 가게입니다.");

            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외가 발생해야 한다.")
        void createStore_fail_when_category_not_found() {
            // given
            StoreRequest request = createStoreRequest();
            when(storeRepository.existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(
                    any(), any(), any())).thenReturn(false);
            when(categoryRepository.findById(any())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeService.createStore(1L, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("카테고리를 찾을 수 없습니다.");

            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 지역이면 예외가 발생해야 한다.")
        void createStore_fail_when_region_not_found() {
            // given
            StoreRequest request = createStoreRequest();
            when(storeRepository.existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(
                    any(), any(), any())).thenReturn(false);
            when(categoryRepository.findById(any())).thenReturn(Optional.of(mock(com.delivery.domain.store.entity.Category.class)));
            when(regionRepository.findById(any())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeService.createStore(1L, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("지역을 찾을 수 없습니다.");

            verify(storeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("가게 조회 실패 테스트")
    class GetStore {
        @Test
        @DisplayName("존재하지 않는 가게 조회 시 예외가 발생해야 한다.")
        void getStore_fail_when_not_found() {
            // given
            UUID storeId = UUID.randomUUID();
            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeService.getStore(storeId))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("가게를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("가게 수정 실패 테스트")
    class UpdateStore {
        @Test
        @DisplayName("OWNER가 본인 가게가 아닌 가게 수정 시 예외가 발생해야 한다.")
        void updateStore_fail_when_access_denied() {
            // given
            UUID storeId = UUID.randomUUID();
            Long ownerId = 1L;
            Long otherUserId = 2L;
            StoreRequest request = createStoreRequest();

            Store store = Store.builder()
                    .userId(ownerId)
                    .categoryId(request.categoryId())
                    .regionId(request.regionId())
                    .name(request.name())
                    .address(request.address())
                    .phone(request.phone())
                    .description(request.description())
                    .minOrderAmount(request.minOrderAmount())
                    .build();

            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> storeService.updateStore(storeId, otherUserId, "ROLE_OWNER", request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("해당 가게에 대한 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("가게 삭제 실패 테스트")
    class DeleteStore {
        @Test
        @DisplayName("OWNER가 본인 가게가 아닌 가게 삭제 시 예외가 발생해야 한다.")
        void deleteStore_fail_when_access_denied() {
            // given
            UUID storeId = UUID.randomUUID();
            Long ownerId = 1L;
            Long otherUserId = 2L;

            Store store = Store.builder()
                    .userId(ownerId)
                    .categoryId(UUID.randomUUID())
                    .regionId(UUID.randomUUID())
                    .name("테스트 가게")
                    .address("서울")
                    .phone("01012345678")
                    .minOrderAmount(10000)
                    .build();

            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> storeService.deleteStore(storeId, otherUserId, "ROLE_OWNER", "2_otheruser"))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("해당 가게에 대한 권한이 없습니다.");
        }
    }
}