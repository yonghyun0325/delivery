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
    Page<Review> findAllByStoreIdAndDeletedAtIsNull(
            UUID storeId,
            Pageable pageable);

    // 로그인 사용자의 삭제되지 않은 리뷰 목록 정렬 조회
    List<Review> findAllByUserIdAndDeletedAtIsNull(
            Long userId,
            Sort sort);

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
    Double findAverageRatingByStoreId(
            @Param("storeId") UUID storeId);
}