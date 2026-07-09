package com.delivery.domain.store.controller;

import com.delivery.domain.store.dto.StoreRequestDto;
import com.delivery.domain.store.dto.StoreResponseDto;
import com.delivery.domain.store.service.StoreService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<?> createStore(
            @Valid @RequestBody StoreRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        StoreResponseDto response = storeService.createStore(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 201);
        result.put("message", "가게 등록 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.status(201).body(result);
    }

    @GetMapping
    public ResponseEntity<?> getStores(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        Page<StoreResponseDto> response = storeService.getStores(categoryId, regionId, name, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "조회 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStore(@PathVariable UUID storeId) {
        StoreResponseDto response = storeService.getStore(storeId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "조회 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PutMapping("/{storeId}")
    public ResponseEntity<?> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        StoreResponseDto response = storeService.updateStore(storeId, userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "가게 수정 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<?> updateStoreStatus(
            @PathVariable UUID storeId,
            @RequestBody Map<String, Boolean> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        StoreResponseDto response = storeService.updateStoreStatus(storeId, userId, request.get("isOpen"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "영업상태 변경 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<?> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        storeService.deleteStore(storeId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "가게 삭제 성공");
        result.put("data", null);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }
}