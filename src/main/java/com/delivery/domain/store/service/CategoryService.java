package com.delivery.domain.store.service;

import com.delivery.domain.store.dto.CategoryRequestDto;
import com.delivery.domain.store.dto.CategoryResponseDto;
import com.delivery.domain.store.entity.Category;
import com.delivery.domain.store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("이미 등록된 카테고리입니다.");
        }

        Category category = Category.builder()
                .name(request.getName())
                .build();

        return CategoryResponseDto.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getCategories() {
        return categoryRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDto updateCategory(UUID categoryId, CategoryRequestDto request) {
        Category category = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("이미 등록된 카테고리입니다.");
        }

        category.update(request.getName());
        return CategoryResponseDto.from(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId, String deletedBy) {
        Category category = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        category.delete(deletedBy);
    }


}