package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);
    List<Category> findAllByDeletedAtIsNull();
}