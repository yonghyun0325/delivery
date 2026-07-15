package com.delivery.domain.store.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.dto.response.RegionResponse;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "지역", description = "지역 CRUD API")
public interface RegionControllerDocs {

    @Operation(summary = "지역 등록", description = "지역을 등록합니다.")
    ResponseEntity<RestApiResponse<RegionResponse>> createRegion(RegionRequest request);

    @Operation(summary = "지역 목록 조회", description = "전체 지역 목록을 조회합니다.")
    ResponseEntity<RestApiResponse<List<RegionResponse>>> getRegions();

    @Operation(summary = "지역 수정", description = "지역 정보를 수정합니다.")
    ResponseEntity<RestApiResponse<RegionResponse>> updateRegion(
            UUID regionId, RegionRequest request);

    @Operation(summary = "지역 삭제", description = "지역을 소프트 삭제 처리합니다.")
    ResponseEntity<RestApiResponse<Void>> deleteRegion(
            UUID regionId, CustomUserDetails userDetails);
}
