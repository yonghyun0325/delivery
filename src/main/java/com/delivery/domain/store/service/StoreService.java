package com.delivery.domain.store.service;

import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final ReviewRepository reviewRepository;
    private final MenuService menuService;

    // 가게 등록
    @Transactional
    public StoreResponse createStore(Long userId, StoreRequest request) {

        if (storeRepository.existsByUserIdAndNameAndRegionIdAndDeletedAtIsNull(
                userId, request.name(), request.regionId())) {
            throw new StoreException(StoreErrorCode.DUPLICATE_STORE);
        }

        categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        regionRepository
                .findById(request.regionId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.REGION_NOT_FOUND));

        Store store =
                Store.builder()
                        .userId(userId)
                        .categoryId(request.categoryId())
                        .regionId(request.regionId())
                        .name(request.name())
                        .address(request.address())
                        .phone(request.phone())
                        .description(request.description())
                        .minOrderAmount(request.minOrderAmount())
                        .isOpen(false)
                        .averageRating(0.0)
                        .build();

        return StoreResponse.from(storeRepository.save(store));
    }

    // 가게 목록 조회
    public Page<StoreResponse> getStores(
            UUID categoryId, UUID regionId, String name, Pageable pageable) {
        return storeRepository
                .findStores(categoryId, regionId, name, pageable)
                .map(StoreResponse::from);
    }

    // 가게 단건 조회
    public StoreResponse getStore(UUID storeId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        return StoreResponse.from(store);
    }

    // 가게 수정
    @Transactional
    public StoreResponse updateStore(UUID storeId, Long userId, String role, StoreRequest request) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (role.equals("ROLE_OWNER") && !store.getUserId().equals(userId)) {
            throw new StoreException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));
        regionRepository
                .findById(request.regionId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.REGION_NOT_FOUND));

        store.update(request);
        return StoreResponse.from(store);
    }

    // 영업상태 변경
    @Transactional
    public StoreResponse updateStoreStatus(UUID storeId, Long userId, String role, Boolean isOpen) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (role.equals("ROLE_OWNER") && !store.getUserId().equals(userId)) {
            throw new StoreException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        store.updateStatus(isOpen);
        return StoreResponse.from(store);
    }

    // 가게 삭제 (Soft Delete)
    @Transactional
    public void deleteStore(UUID storeId, Long userId, String role) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (role.equals("ROLE_OWNER") && !store.getUserId().equals(userId)) {
            throw new StoreException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        menuService.deleteMenusByStoreId(storeId, userId.toString());
        store.delete(userId.toString());
    }

    // 가게 평점 평균
    @Transactional
    public void updateAverageRating(UUID storeId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        Double average = reviewRepository.findAverageRatingByStoreId(storeId);
        store.updateAverageRating(average);
    }
}
