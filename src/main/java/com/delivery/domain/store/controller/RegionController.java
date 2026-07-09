package com.delivery.domain.store.controller;

import com.delivery.domain.store.dto.RegionRequestDto;
import com.delivery.domain.store.dto.RegionResponseDto;
import com.delivery.domain.store.service.RegionService;
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
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<?> createRegion(@Valid @RequestBody RegionRequestDto request) {
        RegionResponseDto response = regionService.createRegion(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 201);
        result.put("message", "지역 등록 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.status(201).body(result);
    }

    @GetMapping
    public ResponseEntity<?> getRegions() {
        List<RegionResponseDto> response = regionService.getRegions();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "조회 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{regionId}")
    public ResponseEntity<?> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionRequestDto request) {
        RegionResponseDto response = regionService.updateRegion(regionId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "지역 수정 성공");
        result.put("data", response);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @DeleteMapping("/{regionId}")
    public ResponseEntity<?> deleteRegion(
            @PathVariable UUID regionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        regionService.deleteRegion(regionId, userDetails.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "지역 삭제 성공");
        result.put("data", null);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }
}