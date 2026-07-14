package com.delivery.domain.ai.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.ai.dto.response.AiLogResponse;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.service.AiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 로그", description = "관리자용 AI 요청/응답 로그 조회 API")
@RestController
@RequiredArgsConstructor
public class AiLogController {

    private final AiLogService aiLogService;

    // size는 10/30/50만 허용(그 외는 10으로 보정), 기본 정렬은 생성일 내림차순
    @Operation(summary = "AI 로그 검색", description = "MANAGER/MASTER가 AI 요청/응답 로그를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "AI 로그 조회 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "403",
                description = "MANAGER/MASTER 권한 없음",
                content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @GetMapping("/api/v1/ai-logs")
    public ResponseEntity<RestApiResponse<Page<AiLogResponse>>> searchLogs(
            @Parameter(description = "요청 타입 필터") @RequestParam(required = false)
                    AiRequestType requestType,
            @Parameter(description = "성공 여부 필터") @RequestParam(required = false) Boolean success,
            @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 - 10/30/50만 허용, 그 외는 10으로 보정")
                    @RequestParam(defaultValue = "10")
                    int size,
            @Parameter(description = "정렬 조건 - 기본 생성일 내림차순, createdAt,asc만 오름차순으로 반전")
                    @RequestParam(required = false)
                    String sort) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "AI 로그 조회에 성공했습니다.",
                        aiLogService.searchLogs(requestType, success, page, size, sort)));
    }
}
