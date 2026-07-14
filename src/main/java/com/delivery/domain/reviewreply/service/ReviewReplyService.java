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
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
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
    private final StoreRepository storeRepository;

    // 사장님 리뷰 답글 등록
    @Transactional
    public ReviewReplyResponse createReply(
            UUID reviewId, ReviewReplyRequest request, Long ownerId) {

        validateReplyRequest(request);

        Review review = findReviewById(reviewId);

        // 현재 로그인한 사장님이 해당 리뷰가 작성된 가게의 실제 소유자인지 검증
        validateStoreOwner(review.getStoreId(), ownerId);

        // 해당 리뷰에 이미 답글이 등록되어 있는지 검증
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
    public ReviewReplyResponse updateReply(
            UUID reviewId, UUID replyId, ReviewReplyRequest request, Long ownerId) {

        validateReplyRequest(request);

        ReviewReply reply = findReplyById(replyId);

        validateReplyBelongsToReview(reply, reviewId);
        validateReplyOwner(reply, ownerId);

        reply.update(request.getContent());

        return ReviewReplyResponse.toDto(reply);
    }

    // 사장님 리뷰 답글 삭제
    @Transactional
    public void deleteReply(UUID reviewId, UUID replyId, Long ownerId, String deletedBy) {

        ReviewReply reply = findReplyById(replyId);

        validateReplyBelongsToReview(reply, reviewId);
        validateReplyOwner(reply, ownerId);

        reply.delete(deletedBy);
    }

    // 리뷰 조회 공통 메서드 (삭제되지 않은 리뷰만 조회)
    private Review findReviewById(UUID reviewId) {
        return reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    // 가게 조회 공통 메서드 (삭제되지 않은 가게만 조회)
    private Store findStoreById(UUID storeId) {
        return storeRepository
                .findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
    }

    // 리뷰 ID 기준 답글 조회 공통 메서드 (삭제되지 않은 답글만 조회)
    private ReviewReply findReplyByReviewId(UUID reviewId) {
        return reviewReplyRepository
                .findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(
                        () ->
                                new ReviewReplyException(
                                        ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND));
    }

    // 답글 ID 기준 답글 조회 공통 메서드 (삭제되지 않은 답글만 조회)
    private ReviewReply findReplyById(UUID replyId) {
        return reviewReplyRepository
                .findByIdAndDeletedAtIsNull(replyId)
                .orElseThrow(
                        () ->
                                new ReviewReplyException(
                                        ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND));
    }

    // 해당 리뷰에 이미 답글이 등록되어 있는지 검증
    private void validateDuplicateReply(UUID reviewId) {
        if (reviewReplyRepository.existsByReviewIdAndDeletedAtIsNull(reviewId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ALREADY_EXISTS);
        }
    }

    // 요청한 리뷰 ID와 답글이 속한 실제 리뷰 ID가 일치하는지 검증
    private void validateReplyBelongsToReview(ReviewReply reply, UUID reviewId) {

        if (!reply.getReview().getId().equals(reviewId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 현재 로그인한 사장님이 답글 작성자인지 검증
    private void validateReplyOwner(ReviewReply reply, Long ownerId) {

        if (!reply.getOwnerId().equals(ownerId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 현재 로그인한 사장님이 해당 가게의 실제 소유자인지 검증
    private void validateStoreOwner(UUID storeId, Long ownerId) {

        Store store = findStoreById(storeId);

        if (!store.getUserId().equals(ownerId)) {
            throw new ReviewReplyException(ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 답글 내용이 비어 있는지 검증
    private void validateReplyRequest(ReviewReplyRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ReviewReplyException(ReviewReplyErrorCode.EMPTY_REPLY_CONTENT);
        }
    }
}
