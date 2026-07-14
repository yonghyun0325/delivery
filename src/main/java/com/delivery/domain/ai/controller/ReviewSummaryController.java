package com.delivery.domain.ai.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.ai.dto.response.ReviewSummaryResponse;
import com.delivery.domain.ai.service.ReviewSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리뷰 요약", description = "가게별 AI 리뷰 요약 조회 API")
@RestController
@RequiredArgsConstructor
public class ReviewSummaryController {

    private final ReviewSummaryService reviewSummaryService;

    @Operation(
            summary = "가게 리뷰 요약 조회",
            description = "리뷰가 10개 이상 쌓인 가게에 한해 매일 갱신되는 AI 리뷰 요약을 조회합니다. 인증 불필요.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공 (리뷰 10개 미만/생성 대기 중/준비 완료 상태를 status 필드로 구분)"),
        @ApiResponse(
                responseCode = "404",
                description = "존재하지 않거나 삭제된 가게",
                content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/api/v1/stores/{storeId}/review-summary")
    public ResponseEntity<RestApiResponse<ReviewSummaryResponse>> getReviewSummary(
            @Parameter(description = "가게 ID", required = true) @PathVariable UUID storeId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "리뷰 요약 조회에 성공했습니다.",
                        reviewSummaryService.getSummary(storeId)));
    }
}
