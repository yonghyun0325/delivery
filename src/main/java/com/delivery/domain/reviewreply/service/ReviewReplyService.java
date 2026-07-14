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
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
            UUID reviewId,
            ReviewReplyRequest request,
            Long ownerId) {

        log.info(
                "리뷰 답글 등록 요청 - reviewId={}, ownerId={}",
                reviewId,
                ownerId);

        // 답글 내용 검증
        validateReplyRequest(request);

        // 삭제되지 않은 리뷰 조회
        Review review = findReviewById(reviewId);

        // 현재 로그인한 사장님이 해당 리뷰가 작성된 가게의 실제 소유자인지 검증
        validateStoreOwner(review.getStoreId(), ownerId);

        // 해당 리뷰에 이미 답글이 등록되어 있는지 검증
        validateDuplicateReply(reviewId);

        // 답글 엔티티 생성 및 저장
        ReviewReply reply =
                new ReviewReply(
                        review,
                        request.getContent(),
                        ownerId);

        ReviewReply savedReply =
                reviewReplyRepository.save(reply);

        log.info(
                "리뷰 답글 등록 완료 - replyId={}, reviewId={}, ownerId={}",
                savedReply.getId(),
                reviewId,
                ownerId);

        return ReviewReplyResponse.toDto(savedReply);
    }

    // 사장님 리뷰 답글 조회
    public ReviewReplyResponse getReply(UUID reviewId) {

        // 삭제되지 않은 답글 조회
        ReviewReply reply = findReplyByReviewId(reviewId);

        return ReviewReplyResponse.toDto(reply);
    }

    // 사장님 리뷰 답글 수정
    @Transactional
    public ReviewReplyResponse updateReply(
            UUID reviewId,
            UUID replyId,
            ReviewReplyRequest request,
            Long ownerId) {

        log.info(
                "리뷰 답글 수정 요청 - reviewId={}, replyId={}, ownerId={}",
                reviewId,
                replyId,
                ownerId);

        // 답글 내용 검증
        validateReplyRequest(request);

        // 삭제되지 않은 답글 조회
        ReviewReply reply = findReplyById(replyId);

        // 요청 리뷰 ID와 실제 답글의 리뷰 ID 일치 여부 검증
        validateReplyBelongsToReview(reply, reviewId);

        // 답글 작성자 본인인지 검증
        validateReplyOwner(reply, ownerId);

        // Dirty Checking을 통해 답글 내용 수정
        reply.update(request.getContent());

        log.info(
                "리뷰 답글 수정 완료 - reviewId={}, replyId={}",
                reviewId,
                replyId);

        return ReviewReplyResponse.toDto(reply);
    }

    // 사장님 리뷰 답글 삭제
    @Transactional
    public void deleteReply(
            UUID reviewId,
            UUID replyId,
            Long ownerId,
            String deletedBy) {

        log.info(
                "리뷰 답글 삭제 요청 - reviewId={}, replyId={}, ownerId={}",
                reviewId,
                replyId,
                ownerId);

        // 삭제되지 않은 답글 조회
        ReviewReply reply = findReplyById(replyId);

        // 요청 리뷰 ID와 실제 답글의 리뷰 ID 일치 여부 검증
        validateReplyBelongsToReview(reply, reviewId);

        // 답글 작성자 본인인지 검증
        validateReplyOwner(reply, ownerId);

        // 답글 소프트 삭제
        reply.delete(deletedBy);

        log.info(
                "리뷰 답글 삭제 완료 - reviewId={}, replyId={}",
                reviewId,
                replyId);
    }

    // 리뷰 조회 공통 메서드
    private Review findReviewById(UUID reviewId) {

        return reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 답글 처리 실패 - 존재하지 않거나 삭제된 리뷰, reviewId={}",
                                    reviewId);

                            return new ReviewException(
                                    ReviewErrorCode.REVIEW_NOT_FOUND);
                        });
    }

    // 가게 조회 공통 메서드
    private Store findStoreById(UUID storeId) {

        return storeRepository
                .findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 답글 처리 실패 - 존재하지 않거나 삭제된 가게, storeId={}",
                                    storeId);

                            return new StoreException(
                                    StoreErrorCode.STORE_NOT_FOUND);
                        });
    }

    // 리뷰 ID 기준 답글 조회 공통 메서드
    private ReviewReply findReplyByReviewId(UUID reviewId) {

        return reviewReplyRepository
                .findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 답글 조회 실패 - 존재하지 않거나 삭제된 답글, reviewId={}",
                                    reviewId);

                            return new ReviewReplyException(
                                    ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND);
                        });
    }

    // 답글 ID 기준 답글 조회 공통 메서드
    private ReviewReply findReplyById(UUID replyId) {

        return reviewReplyRepository
                .findByIdAndDeletedAtIsNull(replyId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 답글 조회 실패 - 존재하지 않거나 삭제된 답글, replyId={}",
                                    replyId);

                            return new ReviewReplyException(
                                    ReviewReplyErrorCode.REVIEW_REPLY_NOT_FOUND);
                        });
    }

    // 해당 리뷰에 이미 답글이 등록되어 있는지 검증
    private void validateDuplicateReply(UUID reviewId) {

        if (reviewReplyRepository.existsByReviewIdAndDeletedAtIsNull(reviewId)) {

            log.warn(
                    "중복 리뷰 답글 등록 시도 - reviewId={}",
                    reviewId);

            throw new ReviewReplyException(
                    ReviewReplyErrorCode.REVIEW_REPLY_ALREADY_EXISTS);
        }
    }

    // 요청한 리뷰 ID와 답글이 속한 실제 리뷰 ID가 일치하는지 검증
    private void validateReplyBelongsToReview(
            ReviewReply reply,
            UUID reviewId) {

        UUID actualReviewId = reply.getReview().getId();

        if (!actualReviewId.equals(reviewId)) {

            log.warn(
                    "리뷰 답글 접근 권한 없음 - replyId={}, requestReviewId={}, actualReviewId={}",
                    reply.getId(),
                    reviewId,
                    actualReviewId);

            throw new ReviewReplyException(
                    ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 현재 로그인한 사장님이 답글 작성자인지 검증
    private void validateReplyOwner(
            ReviewReply reply,
            Long ownerId) {

        if (!reply.getOwnerId().equals(ownerId)) {

            log.warn(
                    "리뷰 답글 작성자 불일치 - replyId={}, loginOwnerId={}, replyOwnerId={}",
                    reply.getId(),
                    ownerId,
                    reply.getOwnerId());

            throw new ReviewReplyException(
                    ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 현재 로그인한 사장님이 해당 가게의 실제 소유자인지 검증
    private void validateStoreOwner(
            UUID storeId,
            Long ownerId) {

        Store store = findStoreById(storeId);

        if (!store.getUserId().equals(ownerId)) {

            log.warn(
                    "리뷰 답글 등록 권한 없음 - storeId={}, loginOwnerId={}, storeOwnerId={}",
                    storeId,
                    ownerId,
                    store.getUserId());

            throw new ReviewReplyException(
                    ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED);
        }
    }

    // 답글 내용이 비어 있는지 검증
    private void validateReplyRequest(ReviewReplyRequest request) {

        if (request.getContent() == null
                || request.getContent().isBlank()) {

            log.warn("리뷰 답글 요청 검증 실패 - 답글 내용이 비어 있음");

            throw new ReviewReplyException(
                    ReviewReplyErrorCode.EMPTY_REPLY_CONTENT);
        }
    }
}