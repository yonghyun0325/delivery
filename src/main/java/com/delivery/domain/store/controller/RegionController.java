package com.delivery.domain.store.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.dto.response.RegionResponse;
import com.delivery.domain.store.service.RegionService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<RegionResponse>> createRegion(@Valid @RequestBody RegionRequest request) {
        RegionResponse response = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "지역 등록 성공", response));
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<List<RegionResponse>>> getRegions() {
        List<RegionResponse> response = regionService.getRegions();
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "조회 성공", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{regionId}")
    public ResponseEntity<RestApiResponse<RegionResponse>> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionRequest request) {
        RegionResponse response = regionService.updateRegion(regionId, request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "지역 수정 성공", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @DeleteMapping("/{regionId}")
    public ResponseEntity<RestApiResponse<Void>> deleteRegion(
            @PathVariable UUID regionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        regionService.deleteRegion(regionId, userDetails.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "지역 삭제 성공", null));
    }
}