package com.delivery.domain.store.service;

import com.delivery.domain.store.dto.request.CategoryRequest;
import com.delivery.domain.store.dto.response.CategoryResponse;
import com.delivery.domain.store.entity.Category;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new StoreException(StoreErrorCode.DUPLICATE_CATEGORY);
        }

        Category category = Category.builder().name(request.name()).build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByDeletedAtIsNull().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryRequest request) {
        Category category =
                categoryRepository
                        .findByCategoryIdAndDeletedAtIsNull(categoryId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new StoreException(StoreErrorCode.DUPLICATE_CATEGORY);
        }

        category.update(request.name());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId, String deletedBy) {
        Category category =
                categoryRepository
                        .findByCategoryIdAndDeletedAtIsNull(categoryId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        category.delete(deletedBy);
    }
}
