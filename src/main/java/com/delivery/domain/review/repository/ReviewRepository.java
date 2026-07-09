package com.delivery.domain.review.repository;

import com.delivery.domain.review.entity.Review;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByStoreId(UUID storeId);

    List<Review> findAllByUserId(UUID userId);

    @Query(
            """
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.storeId = :storeId
            """)
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);
}
