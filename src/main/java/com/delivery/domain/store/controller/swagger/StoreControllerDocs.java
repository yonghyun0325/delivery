package com.delivery.domain.store.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.request.StoreStatusRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.enums.StoreSortType;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가게", description = "가게 CRUD API")
public interface StoreControllerDocs {

    @Operation(summary = "가게 등록", description = "가게를 등록합니다.")
    ResponseEntity<RestApiResponse<StoreResponse>> createStore(
            StoreRequest request, CustomUserDetails userDetails);

    @Operation(summary = "가게 목록 조회", description = "가게 목록을 검색 조건으로 조회합니다.")
    ResponseEntity<RestApiResponse<Page<StoreResponse>>> getStores(
            UUID categoryId,
            UUID regionId,
            String name,
            StoreSortType sortType,
            Pageable pageable);

    @Operation(summary = "가게 단건 조회", description = "가게 상세 정보를 조회합니다.")
    ResponseEntity<RestApiResponse<StoreResponse>> getStore(UUID storeId);

    @Operation(summary = "가게 수정", description = "가게 정보를 수정합니다.")
    ResponseEntity<RestApiResponse<StoreResponse>> updateStore(
            UUID storeId, StoreRequest request, CustomUserDetails userDetails);

    @Operation(summary = "영업 상태 변경", description = "가게의 영업 상태를 변경합니다.")
    ResponseEntity<RestApiResponse<StoreResponse>> updateStoreStatus(
            UUID storeId, StoreStatusRequest request, CustomUserDetails userDetails);

    @Operation(summary = "가게 삭제", description = "가게를 소프트 삭제 처리합니다.")
    ResponseEntity<RestApiResponse<Void>> deleteStore(UUID storeId, CustomUserDetails userDetails);
}
