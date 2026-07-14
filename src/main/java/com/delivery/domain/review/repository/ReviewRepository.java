package com.delivery.domain.review.repository;

import com.delivery.domain.review.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 삭제되지 않은 리뷰 단건 조회
    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    // 가게 삭제 시 리뷰 일괄 소프트 삭제용
    List<Review> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    // 음식점의 삭제되지 않은 리뷰 페이징 및 정렬 조회
    Page<Review> findAllByStoreIdAndDeletedAtIsNull(UUID storeId, Pageable pageable);

    // 로그인 사용자의 삭제되지 않은 리뷰 목록 정렬 조회
    List<Review> findAllByUserIdAndDeletedAtIsNull(Long userId, Sort sort);

    // 주문당 리뷰 중복 등록 여부 확인
    boolean existsByOrderId(UUID orderId);

    // 음식점의 삭제되지 않은 리뷰 평균 평점 조회
    @Query(
            """
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.storeId = :storeId
              AND r.deletedAt IS NULL
            """)
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);

    long countByStoreIdAndDeletedAtIsNull(UUID storeId);

    // AI 리뷰 요약 대상 가게 목록 - 삭제되지 않은 리뷰가 threshold개 이상인 가게 storeId만 반환
    @Query(
            """
            SELECT r.storeId
            FROM Review r
            WHERE r.deletedAt IS NULL
            GROUP BY r.storeId
            HAVING COUNT(r) >= :threshold
            """)
    List<UUID> findStoreIdsWithReviewCountAtLeast(@Param("threshold") long threshold);

    // AI 리뷰 요약 프롬프트 구성용 - 리뷰가 아무리 쌓여도 프롬프트 크기(비용/응답속도)가 고정되도록
    // 최신순 상위 N개만 조회
    List<Review> findTop50ByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID storeId);
}
