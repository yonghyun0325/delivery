package com.delivery.domain.reviewreply.service;

import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.exception.ReviewErrorCode;
import com.delivery.domain.review.exception.ReviewException;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.domain.reviewreply.entity.ReviewReply;
import com.delivery.domain.reviewreply.exception.ReviewReplyErrorCode;
import com.delivery.domain.reviewreply.exception.ReviewReplyException;
import com.delivery.domain.reviewreply.repository.ReviewReplyRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReplyService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;

    // 사장님 리뷰 답글 등록
    @Transactional
    public ReviewReplyResponse createReply(UUID reviewId, ReviewReplyRequest request, UUID ownerId) {
        validateReplyRequest(request);

        Review review = findReviewById(reviewId);

        validateDuplicateReply(reviewId);

        ReviewReply reply = new ReviewReply(review, request.getContent(), ownerId);
        ReviewReply savedReply = reviewReplyRepository.save(reply);

        return ReviewReplyResponse.toDto(savedReply);
    }

    // 사장님 리뷰 답글 조회
    public ReviewReplyResponse getReply(UUID reviewId) {
        ReviewReply reply = findReplyByReviewId(reviewId);

        return ReviewReplyResponse.toDto(reply);
    }

    // 사장님 리뷰 답글 수정
    @Transactional
    public ReviewReplyResponse updateReply(UUID reviewId, UUID replyId, ReviewReplyRequest request) {
        validateReplyRequest(request);

        ReviewReply reply = findReplyById(replyId);

        validateReplyBelongsToReview(reply, reviewId);

        reply.update(request.getContent());

        return ReviewReplyResponse.toDto(reply);
    }

    // 사장님 리뷰 답글 삭제
    @Transactional
    public void deleteReply(UUID reviewId, UUID replyId, String deletedBy) {
        ReviewReply reply = findReplyById(replyId);

        validateReplyBelongsToReview(reply, reviewId);

        reply.delete(deletedBy);
    }

    // 리뷰 조회 공통 메서드
    private Review findReviewById(UUID reviewId) {
        return reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    // 리뷰 ID 기준 답글 조회 공통 메서드
    private ReviewReply findReplyByReviewId(UUID reviewId) {
        return reviewReplyRepository
                .findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(
                        () -> new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND));
    }

    // 답글 ID 기준 답글 조회 공통 메서드
    private ReviewReply findReplyById(UUID replyId) {
        return reviewReplyRepository
                .findByIdAndDeletedAtIsNull(replyId)
                .orElseThrow(
                        () -> new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND));
    }

    // 리뷰에 이미 답글이 있는지 검증
    private void validateDuplicateReply(UUID reviewId) {
        if (reviewReplyRepository.existsByReviewIdAndDeletedAtIsNull(reviewId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ALREADY_EXISTS);
        }
    }

    // 요청으로 들어온 reviewId와 replyId의 실제 reviewId가 일치하는지 검증
    private void validateReplyBelongsToReview(ReviewReply reply, UUID reviewId) {
        if (!reply.getReview().getId().equals(reviewId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 답글 내용 검증
    private void validateReplyRequest(ReviewReplyRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ReviewReplyException(ReviewReplyErrorCode.EMPTY_REPLY_CONTENT);
        }
    }
}