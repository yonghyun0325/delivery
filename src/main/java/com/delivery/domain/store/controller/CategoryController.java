package com.delivery.domain.store.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.CategoryRequest;
import com.delivery.domain.store.dto.response.CategoryResponse;
import com.delivery.domain.store.service.CategoryService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "카테고리 등록 성공", response));
    }


    @GetMapping
    public ResponseEntity<RestApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> response = categoryService.getCategories();
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "조회 성공", response));
    }


    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<RestApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "카테고리 수정 성공", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<RestApiResponse<Void>> deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryService.deleteCategory(categoryId, userDetails.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "카테고리 삭제 성공", null));
    }
}