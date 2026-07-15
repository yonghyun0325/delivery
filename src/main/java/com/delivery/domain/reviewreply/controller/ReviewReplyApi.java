package com.delivery.domain.reviewreply.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "리뷰 답글", description = "음식점 사장님의 리뷰 답글 관리 API")
public interface ReviewReplyApi {

    @Operation(summary = "리뷰 답글 등록", description = "해당 음식점의 실제 사장님이 리뷰에 답글을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 답글 등록 성공"),
        @ApiResponse(responseCode = "400", description = "답글 내용이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "해당 음식점의 사장님이 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰 또는 음식점을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 해당 리뷰에 답글이 존재함")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<ReviewReplyResponse>> createReply(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "답글을 등록할 리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewReplyRequest request);

    @Operation(summary = "리뷰 답글 조회", description = "리뷰 ID를 기준으로 삭제되지 않은 사장님 답글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 조회 성공"),
        @ApiResponse(responseCode = "404", description = "리뷰 답글을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<ReviewReplyResponse>> getReply(
            @Parameter(description = "답글을 조회할 리뷰 ID", required = true) @PathVariable UUID reviewId);

    @Operation(summary = "리뷰 답글 수정", description = "답글을 작성한 사장님 본인이 답글 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "답글 내용이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "답글 작성자가 아니거나 리뷰 정보가 일치하지 않음"),
        @ApiResponse(responseCode = "404", description = "리뷰 답글을 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<ReviewReplyResponse>> updateReply(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "답글이 작성된 리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "수정할 답글 ID", required = true) @PathVariable UUID replyId,
            @Valid @RequestBody ReviewReplyRequest request);

    @Operation(summary = "리뷰 답글 삭제", description = "답글을 작성한 사장님 본인이 답글을 소프트 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "답글 작성자가 아니거나 리뷰 정보가 일치하지 않음"),
        @ApiResponse(responseCode = "404", description = "리뷰 답글을 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<Void>> deleteReply(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "답글이 작성된 리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "삭제할 답글 ID", required = true) @PathVariable UUID replyId);
}
