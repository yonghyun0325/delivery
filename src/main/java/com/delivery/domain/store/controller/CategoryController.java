package com.delivery.domain.store.controller;

import com.delivery.domain.store.dto.CategoryRequestDto;
import com.delivery.domain.store.dto.CategoryResponseDto;
import com.delivery.domain.store.service.CategoryService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.delivery.global.security.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequestDto request) {
        CategoryResponseDto response = categoryService.createCategory(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 201);
        result.put("message", "카테고리 등록 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.status(201).body(result);
    }

    @GetMapping
    public ResponseEntity<?> getCategories() {
        List<CategoryResponseDto> response = categoryService.getCategories();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "조회 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequestDto request) {
        CategoryResponseDto response = categoryService.updateCategory(categoryId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "카테고리 수정 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryService.deleteCategory(categoryId, userDetails.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "카테고리 삭제 성공");
        result.put("data", null);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }
}