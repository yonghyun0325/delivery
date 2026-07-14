package com.delivery.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.entity.Category;
import com.delivery.domain.store.entity.Region;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.config.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "gemini.api-key=test-dummy-key",
        "gemini.base-url=https://generativelanguage.googleapis.com",
        "gemini.model=gemini-1.5-flash"
})
class StoreServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private StoreService storeService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private RegionRepository regionRepository;

    private Category savedCategory;
    private Region savedRegion;

    @BeforeEach
    void setUp() {
        savedCategory = categoryRepository.save(
                Category.builder().name("한식").build());
        savedRegion = regionRepository.save(
                Region.builder().name("강남구").latitude(37.5).longitude(127.0).build());
    }

    @Test
    @Transactional
    @DisplayName("가게 등록 성공")
    void createStore_success() {
        // given
        StoreRequest request = new StoreRequest(
                savedCategory.getCategoryId(),
                savedRegion.getRegionId(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트 가게입니다",
                10000);

        // when
        StoreResponse response = storeService.createStore(1L, request);

        // then
        assertThat(response.name()).isEqualTo("테스트 가게");
        assertThat(response.address()).isEqualTo("서울시 강남구");
        assertThat(response.isOpen()).isFalse();
        assertThat(response.averageRating()).isEqualTo(0.0);
    }

    @Test
    @Transactional
    @DisplayName("중복된 가게 등록 시 예외가 발생해야 한다.")
    void createStore_fail_when_duplicate() {
        // given
        StoreRequest request = new StoreRequest(
                savedCategory.getCategoryId(),
                savedRegion.getRegionId(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트",
                10000);

        storeService.createStore(1L, request);

        // when & then
        assertThatThrownBy(() -> storeService.createStore(1L, request))
                .isInstanceOf(StoreException.class)
                .hasMessage("이미 등록된 가게입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 가게 조회 시 예외가 발생해야 한다.")
    void getStore_fail_when_not_found() {
        // given
        UUID randomId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> storeService.getStore(randomId))
                .isInstanceOf(StoreException.class)
                .hasMessage("가게를 찾을 수 없습니다.");
    }
}