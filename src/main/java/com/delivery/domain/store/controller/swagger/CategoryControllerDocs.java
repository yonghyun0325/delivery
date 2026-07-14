package com.delivery.domain.store.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.CategoryRequest;
import com.delivery.domain.store.dto.response.CategoryResponse;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "카테고리", description = "카테고리 CRUD API")
public interface CategoryControllerDocs {

    @Operation(summary = "카테고리 등록", description = "카테고리를 등록합니다.")
    ResponseEntity<RestApiResponse<CategoryResponse>> createCategory(CategoryRequest request);

    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    ResponseEntity<RestApiResponse<List<CategoryResponse>>> getCategories();

    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정합니다.")
    ResponseEntity<RestApiResponse<CategoryResponse>> updateCategory(UUID categoryId, CategoryRequest request);

    @Operation(summary = "카테고리 삭제", description = "카테고리를 소프트 삭제 처리합니다.")
    ResponseEntity<RestApiResponse<Void>> deleteCategory(UUID categoryId, CustomUserDetails userDetails);
}