package com.delivery.domain.store.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.dto.response.RegionResponse;
import com.delivery.domain.store.service.RegionService;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "지역", description = "지역 CRUD API")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역 등록", description = "지역을 등록합니다.")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<RegionResponse>> createRegion(@Valid @RequestBody RegionRequest request) {
        RegionResponse response = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "지역 등록 성공", response));
    }

    @Operation(summary = "지역 목록 조회", description = "전체 지역 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<RestApiResponse<List<RegionResponse>>> getRegions() {
        List<RegionResponse> response = regionService.getRegions();
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "조회 성공", response));
    }

    @Operation(summary = "지역 수정", description = "지역 정보를 수정합니다.")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{regionId}")
    public ResponseEntity<RestApiResponse<RegionResponse>> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionRequest request) {
        RegionResponse response = regionService.updateRegion(regionId, request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "지역 수정 성공", response));
    }

    @Operation(summary = "지역 삭제", description = "지역을 소프트 삭제 처리합니다.")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @DeleteMapping("/{regionId}")
    public ResponseEntity<RestApiResponse<Void>> deleteRegion(
            @PathVariable UUID regionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        regionService.deleteRegion(regionId, userDetails.getId() + "_" + userDetails.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "지역 삭제 성공", null));
    }
}