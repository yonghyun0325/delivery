package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreRepositoryCustom {
    Page<Store> searchStores(UUID categoryId, String name, Pageable pageable);
}