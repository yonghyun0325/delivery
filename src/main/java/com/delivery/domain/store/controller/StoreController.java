package com.delivery.domain.store.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.controller.swagger.StoreControllerDocs;
import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.request.StoreStatusRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.service.StoreService;
import com.delivery.domain.store.enums.StoreSortType;
import com.delivery.domain.user.entity.Role;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController implements StoreControllerDocs {

    private final StoreService storeService;

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody StoreRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreResponse response = storeService.createStore(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "가게 등록 성공", response));
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<Page<StoreResponse>>> getStores(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StoreSortType sortType,
            Pageable pageable) {
        Page<StoreResponse> response = storeService.getStores(categoryId, regionId, name, sortType, pageable);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "조회 성공", response));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<RestApiResponse<StoreResponse>> getStore(@PathVariable UUID storeId) {
        StoreResponse response = storeService.getStore(storeId);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "조회 성공", response));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PutMapping("/{storeId}")
    public ResponseEntity<RestApiResponse<StoreResponse>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreResponse response =
                storeService.updateStore(
                        storeId, userDetails.getId(), hasElevatedRole(userDetails), request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "가게 수정 성공", response));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<RestApiResponse<StoreResponse>> updateStoreStatus(
            @PathVariable UUID storeId,
            @RequestBody StoreStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreResponse response =
                storeService.updateStoreStatus(
                        storeId,
                        userDetails.getId(),
                        hasElevatedRole(userDetails),
                        request.isOpen());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "영업상태 변경 성공", response));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<RestApiResponse<Void>> deleteStore(
            @PathVariable UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeService.deleteStore(
                storeId,
                userDetails.getId(),
                hasElevatedRole(userDetails),
                userDetails.getId() + "_" + userDetails.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "가게 삭제 성공", null));
    }

    private boolean hasElevatedRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(Role.Authority.MANAGER) || a.equals(Role.Authority.MASTER));
    }
}
