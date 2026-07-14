package com.delivery.domain.review.repository;

import com.delivery.domain.review.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    List<Review> findAllByDeletedAtIsNull();

    List<Review> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    List<Review> findAllByUserIdAndDeletedAtIsNull(Long userId);

    @Query(
            """
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.storeId = :storeId
              AND r.deletedAt IS NULL
            """)
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);
}
