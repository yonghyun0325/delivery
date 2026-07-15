package com.delivery.domain.store.service;

import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.review.service.ReviewService;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.store.enums.StoreSortType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<StoreResponse> getStores(UUID categoryId, UUID regionId, String name, StoreSortType sortType, Pageable pageable) {
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }
        Pageable validatedPageable = PageRequest.of(pageable.getPageNumber(), size);
        return storeRepository.searchStores(categoryId, regionId, name, sortType, validatedPageable)
                .map(StoreResponse::from);
    }

    public StoreResponse getStore(UUID storeId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        return StoreResponse.from(store);
    }

    // 가게가 삭제되지 않고 존재하는지 여부 - AI 리뷰 요약 등 다른 도메인이 대상 가게 필터링에 사용
    public boolean existsActiveStore(UUID storeId) {
        return storeRepository.findByStoreIdAndDeletedAtIsNull(storeId).isPresent();
    }

    @Transactional
    public StoreResponse updateStore(UUID storeId, Long userId, boolean isElevated, StoreRequest request) {
        Store store = getStoreWithOwnerCheck(storeId, userId, isElevated);
        categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));
        regionRepository
                .findById(request.regionId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.REGION_NOT_FOUND));
        store.update(request);
        return StoreResponse.from(store);
    }

    @Transactional
    public StoreResponse updateStoreStatus(UUID storeId, Long userId, boolean isElevated, Boolean isOpen) {
        Store store = getStoreWithOwnerCheck(storeId, userId, isElevated);
        store.updateStatus(isOpen);
        return StoreResponse.from(store);
    }

    @Transactional
    public void deleteStore(UUID storeId, Long userId, boolean isElevated, String deletedBy) {
        Store store = getStoreWithOwnerCheck(storeId, userId, isElevated);
        menuService.deleteMenusByStoreId(storeId, deletedBy);
        List<Review> reviews = reviewRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);
        reviews.forEach(review -> review.delete(deletedBy));
        store.delete(deletedBy);
    }

    @Transactional
    public void updateAverageRating(UUID storeId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        Double average = reviewRepository.findAverageRatingByStoreId(storeId);
        store.updateAverageRating(average);
    }

    private Store getStoreWithOwnerCheck(UUID storeId, Long userId, boolean isElevated) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        if (!isElevated && !store.getUserId().equals(userId)) {
            throw new StoreException(StoreErrorCode.STORE_ACCESS_DENIED);
        }
        return store;
    }
}