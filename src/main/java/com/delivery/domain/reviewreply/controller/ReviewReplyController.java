package com.delivery.domain.reviewreply.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.domain.reviewreply.service.ReviewReplyService;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리뷰 답글", description = "사장님 리뷰 답글 관리 API")
@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/replies")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyService reviewReplyService;

    // 사장님 리뷰 답글 등록
    @Operation(summary = "리뷰 답글 등록", description = "사장님이 특정 리뷰에 답글을 등록합니다. 리뷰당 답글은 하나만 등록할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 답글 등록 성공"),
        @ApiResponse(responseCode = "400", description = "답글 내용이 올바르지 않음"),
        @ApiResponse(responseCode = "403", description = "해당 가게의 사장님이 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 해당 리뷰에 답글이 존재함")
    })
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> createReply(
            @Parameter(description = "답글을 등록할 리뷰 ID", required = true) @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewReplyRequest request) {

        Long ownerId = userDetails.getId();

        ReviewReplyResponse response = reviewReplyService.createReply(reviewId, request, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "사장님 리뷰 답글 등록 성공", response));
    }

    // 사장님 리뷰 답글 조회
    @Operation(summary = "리뷰 답글 조회", description = "특정 리뷰에 등록된 사장님 답글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 조회 성공"),
        @ApiResponse(responseCode = "404", description = "리뷰 또는 답글을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> getReply(
            @Parameter(description = "답글을 조회할 리뷰 ID", required = true) @PathVariable
                    UUID reviewId) {

        ReviewReplyResponse response = reviewReplyService.getReply(reviewId);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 조회 성공", response));
    }

    // 사장님 리뷰 답글 수정
    @Operation(summary = "리뷰 답글 수정", description = "사장님이 기존 리뷰 답글의 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "답글 내용이 올바르지 않음"),
        @ApiResponse(responseCode = "403", description = "답글 수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰 또는 답글을 찾을 수 없음")
    })
    @PatchMapping("/{replyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> updateReply(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "수정할 답글 ID", required = true) @PathVariable UUID replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewReplyRequest request) {

        Long ownerId = userDetails.getId();

        ReviewReplyResponse response =
                reviewReplyService.updateReply(reviewId, replyId, request, ownerId);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 수정 성공", response));
    }

    // 사장님 리뷰 답글 삭제
    @Operation(summary = "리뷰 답글 삭제", description = "사장님이 특정 리뷰에 등록된 답글을 소프트 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 답글 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "답글 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰 또는 답글을 찾을 수 없음")
    })
    @DeleteMapping("/{replyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestApiResponse<Void>> deleteReply(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "삭제할 답글 ID", required = true) @PathVariable UUID replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long ownerId = userDetails.getId();
        String deletedBy = userDetails.getUsername();

        reviewReplyService.deleteReply(reviewId, replyId, ownerId, deletedBy);

        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 삭제 성공", null));
    }
}
