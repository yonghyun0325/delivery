package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndDeletedAtIsNullAndCategoryIdNot(String name, UUID categoryId);

    List<Category> findAllByDeletedAtIsNull();

    Optional<Category> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
