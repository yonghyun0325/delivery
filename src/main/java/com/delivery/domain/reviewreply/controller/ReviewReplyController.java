package com.delivery.domain.reviewreply.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.domain.reviewreply.service.ReviewReplyService;
import com.delivery.global.security.config.CustomUserDetails;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews/{reviewId}/replies")
public class ReviewReplyController implements ReviewReplyApi {

    private final ReviewReplyService reviewReplyService;

    // 해당 가게의 실제 사장님만 답글 등록
    @Override
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> createReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewReplyRequest request) {

        ReviewReplyResponse response =
                reviewReplyService.createReply(
                        reviewId,
                        request,
                        userDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED,
                                "리뷰 답글 등록에 성공했습니다.",
                                response));
    }

    // 리뷰 답글 조회
    @Override
    @GetMapping
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> getReply(
            @PathVariable UUID reviewId) {

        ReviewReplyResponse response =
                reviewReplyService.getReply(reviewId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "리뷰 답글 조회에 성공했습니다.",
                        response));
    }

    // 답글 작성자 본인만 수정
    @Override
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{replyId}")
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> updateReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @PathVariable UUID replyId,
            @Valid @RequestBody ReviewReplyRequest request) {

        ReviewReplyResponse response =
                reviewReplyService.updateReply(
                        reviewId,
                        replyId,
                        request,
                        userDetails.getId());

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "리뷰 답글 수정에 성공했습니다.",
                        response));
    }

    // 답글 작성자 본인만 삭제
    @Override
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{replyId}")
    public ResponseEntity<RestApiResponse<Void>> deleteReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @PathVariable UUID replyId) {

        String deletedBy =
                userDetails.getId()
                        + "_"
                        + userDetails.getUsername();

        reviewReplyService.deleteReply(
                reviewId,
                replyId,
                userDetails.getId(),
                deletedBy);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "리뷰 답글 삭제에 성공했습니다.",
                        null));
    }
}