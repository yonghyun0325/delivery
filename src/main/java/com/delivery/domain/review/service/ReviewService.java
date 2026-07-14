package com.delivery.domain.review.service;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.enums.ReviewSortType;
import com.delivery.domain.review.exception.ReviewErrorCode;
import com.delivery.domain.review.exception.ReviewException;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.service.StoreService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreService storeService;

    // 리뷰 등록
    @Transactional
    public ReviewResponse createReview(Long loginUserId, ReviewRequest request) {

        log.info(
                "리뷰 등록 요청 - userId={}, orderId={}",
                loginUserId,
                request.getOrderId());

        // 리뷰 평점과 내용 검증
        validateReviewRequest(request);

        // 삭제되지 않은 주문 조회
        Order order = findActiveOrderById(request.getOrderId());

        // 주문자 본인 여부와 주문 완료 상태 검증
        validateReviewableOrder(order, loginUserId);

        // 주문당 리뷰는 삭제 여부와 관계없이 하나만 등록 가능
        validateDuplicateReview(order.getId());

        // 주문 정보를 기준으로 리뷰 생성
        Review review =
                Review.create(
                        order.getId(),
                        loginUserId,
                        order.getStoreId(),
                        request.getRating(),
                        request.getContent());

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 신규 리뷰가 반영된 가게 평균 평점 갱신
        storeService.updateAverageRating(savedReview.getStoreId());

        log.info(
                "리뷰 등록 완료 - reviewId={}, storeId={}, rating={}",
                savedReview.getId(),
                savedReview.getStoreId(),
                savedReview.getRating());

        return ReviewResponse.toDto(savedReview);
    }

    // 리뷰 단건 조회
    public ReviewResponse getReview(UUID reviewId) {

        // 삭제되지 않은 리뷰 조회
        Review review = findActiveReviewById(reviewId);

        return ReviewResponse.toDto(review);
    }

    // 음식점 리뷰 목록 페이징 및 정렬 조회
    public Page<ReviewResponse> getReviewsByStore(
            UUID storeId,
            ReviewSortType sortType,
            Pageable pageable) {

        // 리뷰 정렬 조건 생성
        Sort sort = createReviewSort(sortType);

        // 요청받은 페이지 번호와 크기는 유지하고 정렬 조건만 적용
        Pageable sortedPageable =
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sort);

        return reviewRepository
                .findAllByStoreIdAndDeletedAtIsNull(storeId, sortedPageable)
                .map(ReviewResponse::toDto);
    }

    // 로그인한 사용자의 리뷰 목록 최신순 조회
    public List<ReviewResponse> getMyReviews(Long loginUserId) {

        // 작성일이 같은 경우에도 순서를 일정하게 유지하기 위해 ID를 추가 정렬 조건으로 사용
        Sort latestSort =
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id"));

        return reviewRepository
                .findAllByUserIdAndDeletedAtIsNull(loginUserId, latestSort)
                .stream()
                .map(ReviewResponse::toDto)
                .toList();
    }

    // 음식점 평균 평점 조회
    public Double getStoreRating(UUID storeId) {

        Double averageRating =
                reviewRepository.findAverageRatingByStoreId(storeId);

        // 등록된 리뷰가 없는 경우 AVG 결과가 null이므로 0.0 반환
        return averageRating == null ? 0.0 : averageRating;
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponse updateReview(
            UUID reviewId,
            Long loginUserId,
            ReviewRequest request) {

        log.info(
                "리뷰 수정 요청 - reviewId={}, userId={}",
                reviewId,
                loginUserId);

        // 리뷰 평점과 내용 검증
        validateReviewRequest(request);

        // 삭제되지 않은 리뷰 조회
        Review review = findActiveReviewById(reviewId);

        // 리뷰 작성자 본인인지 검증
        validateReviewOwner(review, loginUserId);

        // Dirty Checking을 통해 평점과 내용 수정
        review.update(
                request.getRating(),
                request.getContent());

        // 수정된 리뷰를 기준으로 가게 평균 평점 갱신
        storeService.updateAverageRating(review.getStoreId());

        log.info(
                "리뷰 수정 완료 - reviewId={}, rating={}",
                reviewId,
                review.getRating());

        return ReviewResponse.toDto(review);
    }

    // 리뷰 소프트 삭제
    @Transactional
    public void deleteReview(UUID reviewId, Long loginUserId) {

        log.info(
                "리뷰 삭제 요청 - reviewId={}, userId={}",
                reviewId,
                loginUserId);

        // 삭제되지 않은 리뷰 조회
        Review review = findActiveReviewById(reviewId);

        // 리뷰 작성자 본인인지 검증
        validateReviewOwner(review, loginUserId);

        // BaseEntity의 deletedAt과 deletedBy 값을 변경하여 소프트 삭제
        review.delete(loginUserId.toString());

        // 삭제된 리뷰를 제외하여 가게 평균 평점 갱신
        storeService.updateAverageRating(review.getStoreId());

        log.info(
                "리뷰 삭제 완료 - reviewId={}, storeId={}",
                reviewId,
                review.getStoreId());
    }

    // 가게 삭제 시 해당 가게의 리뷰 전체 소프트 삭제
    @Transactional
    public void deleteReviewsByStoreId(UUID storeId, String deletedBy) {

        log.info(
                "가게 리뷰 일괄 삭제 요청 - storeId={}, deletedBy={}",
                storeId,
                deletedBy);

        // 해당 가게에 등록된 삭제되지 않은 리뷰 조회
        List<Review> reviews =
                reviewRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);

        // 조회한 모든 리뷰를 소프트 삭제
        reviews.forEach(review -> review.delete(deletedBy));

        log.info(
                "가게 리뷰 일괄 삭제 완료 - storeId={}, deletedCount={}",
                storeId,
                reviews.size());
    }

    // 삭제되지 않은 리뷰 조회
    private Review findActiveReviewById(UUID reviewId) {

        return reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 조회 실패 - 존재하지 않거나 삭제된 리뷰, reviewId={}",
                                    reviewId);

                            return new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND);
                        });
    }

    // 삭제되지 않은 주문 조회
    private Order findActiveOrderById(UUID orderId) {

        return orderRepository
                .findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "리뷰 등록 실패 - 존재하지 않거나 삭제된 주문, orderId={}",
                                    orderId);

                            return new ReviewException(ReviewErrorCode.ORDER_NOT_FOUND);
                        });
    }

    // 주문당 리뷰 중복 등록 검증
    private void validateDuplicateReview(UUID orderId) {

        /*
         * 삭제된 리뷰도 포함하여 조회합니다.
         * 따라서 한 번 리뷰를 작성한 주문은 리뷰 삭제 후에도 다시 작성할 수 없습니다.
         */
        if (reviewRepository.existsByOrderId(orderId)) {

            log.warn(
                    "중복 리뷰 등록 시도 - orderId={}",
                    orderId);

            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    // 리뷰 작성자 본인인지 검증
    private void validateReviewOwner(
            Review review,
            Long loginUserId) {

        if (!review.getUserId().equals(loginUserId)) {

            log.warn(
                    "리뷰 접근 권한 없음 - reviewId={}, loginUserId={}, reviewOwnerId={}",
                    review.getId(),
                    loginUserId,
                    review.getUserId());

            throw new ReviewException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }
    }

    // 리뷰 요청 데이터 검증
    private void validateReviewRequest(ReviewRequest request) {

        // 평점은 1점 이상 5점 이하만 허용
        if (request.getRating() == null
                || request.getRating() < 1
                || request.getRating() > 5) {

            log.warn(
                    "리뷰 요청 검증 실패 - 유효하지 않은 평점, rating={}",
                    request.getRating());

            throw new ReviewException(ReviewErrorCode.INVALID_RATING);
        }

        // 리뷰 내용은 null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
        if (request.getContent() == null
                || request.getContent().isBlank()) {

            log.warn("리뷰 요청 검증 실패 - 리뷰 내용이 비어 있음");

            throw new ReviewException(ReviewErrorCode.EMPTY_CONTENT);
        }
    }

    // 리뷰 작성 가능한 주문인지 검증
    private void validateReviewableOrder(
            Order order,
            Long loginUserId) {

        // 로그인 사용자가 실제 주문자인지 검증
        if (!order.getUserId().equals(loginUserId)) {

            log.warn(
                    "리뷰 등록 권한 없음 - orderId={}, loginUserId={}, orderUserId={}",
                    order.getId(),
                    loginUserId,
                    order.getUserId());

            throw new ReviewException(ReviewErrorCode.ORDER_USER_MISMATCH);
        }

        // 배송 완료 또는 주문 최종 완료 상태에서만 리뷰 작성 가능
        if (order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.COMPLETED) {

            log.warn(
                    "리뷰 작성 불가능한 주문 상태 - orderId={}, status={}",
                    order.getId(),
                    order.getStatus());

            throw new ReviewException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    // 리뷰 정렬 조건 생성
    private Sort createReviewSort(ReviewSortType sortType) {

        // 정렬 조건이 전달되지 않은 경우 최신순을 기본값으로 사용
        ReviewSortType resolvedSortType =
                sortType == null ? ReviewSortType.LATEST : sortType;

        return switch (resolvedSortType) {
            // 최신 작성 리뷰 우선
            case LATEST ->
                    Sort.by(
                            Sort.Order.desc("createdAt"),
                            Sort.Order.desc("id"));

            // 오래된 작성 리뷰 우선
            case OLDEST ->
                    Sort.by(
                            Sort.Order.asc("createdAt"),
                            Sort.Order.asc("id"));

            // 평점이 높은 리뷰 우선, 평점이 같으면 최신순
            case RATING_HIGH ->
                    Sort.by(
                            Sort.Order.desc("rating"),
                            Sort.Order.desc("createdAt"),
                            Sort.Order.desc("id"));

            // 평점이 낮은 리뷰 우선, 평점이 같으면 최신순
            case RATING_LOW ->
                    Sort.by(
                            Sort.Order.asc("rating"),
                            Sort.Order.desc("createdAt"),
                            Sort.Order.desc("id"));
        };
    }
}