package com.delivery.domain.store.service;

import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.enums.StoreSortType;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import com.delivery.domain.store.repository.RegionRepository;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final ReviewRepository reviewRepository;
    private final MenuService menuService;

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE = 10;

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

        StoreResponse response = StoreResponse.from(storeRepository.save(store));
        log.info("가게 등록 완료 - storeId={}, userId={}", response.storeId(), userId);
        return response;
    }

    public Page<StoreResponse> getStores(UUID categoryId, UUID regionId, String name, StoreSortType sortType, Pageable pageable) {
        Pageable validatedPageable = PageRequest.of(pageable.getPageNumber(), resolvePageSize(pageable.getPageSize()));
        return storeRepository.searchStores(categoryId, regionId, name, sortType, validatedPageable)
                .map(StoreResponse::from);
    }

    private int resolvePageSize(int requested) {
        return ALLOWED_PAGE_SIZES.contains(requested) ? requested : DEFAULT_PAGE_SIZE;
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
        return storeRepository.existsByStoreIdAndDeletedAtIsNull(storeId);
    }

    @Transactional
    public StoreResponse updateStore(
            UUID storeId, Long userId, boolean isElevated, StoreRequest request) {
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
    public StoreResponse updateStoreStatus(
            UUID storeId, Long userId, boolean isElevated, Boolean isOpen) {
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
        log.info("가게 삭제 완료 - storeId={}, deletedBy={}", storeId, deletedBy);
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
