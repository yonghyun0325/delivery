package com.delivery.domain.store.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.entity.Region;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.RegionRepository;
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
class RegionServiceUnitTest {

    @Mock private RegionRepository regionRepository;
    @InjectMocks private RegionService regionService;

    private RegionRequest createRegionRequest() {
        return new RegionRequest("강남구", 37.5, 127.0);
    }

    @Nested
    @DisplayName("지역 등록 실패 테스트")
    class CreateRegion {

        @Test
        @DisplayName("중복된 지역이면 예외가 발생해야 한다.")
        void createRegion_fail_when_duplicate() {
            RegionRequest request = createRegionRequest();
            when(regionRepository.existsByNameAndDeletedAtIsNull("강남구")).thenReturn(true);

            assertThatThrownBy(() -> regionService.createRegion(request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("이미 등록된 지역입니다.");

            verify(regionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("지역 수정 실패 테스트")
    class UpdateRegion {

        @Test
        @DisplayName("존재하지 않는 지역 수정 시 예외가 발생해야 한다.")
        void updateRegion_fail_when_not_found() {
            UUID regionId = UUID.randomUUID();
            RegionRequest request = createRegionRequest();
            when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> regionService.updateRegion(regionId, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("지역을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("지역 삭제 실패 테스트")
    class DeleteRegion {

        @Test
        @DisplayName("존재하지 않는 지역 삭제 시 예외가 발생해야 한다.")
        void deleteRegion_fail_when_not_found() {
            UUID regionId = UUID.randomUUID();
            when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> regionService.deleteRegion(regionId, "admin"))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("지역을 찾을 수 없습니다.");
        }
    }
}