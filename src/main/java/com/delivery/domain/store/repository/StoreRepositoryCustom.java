package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.enums.StoreSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreRepositoryCustom {
    Page<Store> searchStores(UUID categoryId, UUID regionId, String name, StoreSortType sortType, Pageable pageable);
}