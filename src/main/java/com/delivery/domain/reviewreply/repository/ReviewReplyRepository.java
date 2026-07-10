package com.delivery.domain.reviewreply.repository;

import com.delivery.domain.reviewreply.entity.ReviewReply;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, UUID> {

    // 삭제되지 않은 답글을 리뷰 ID 기준으로 조회
    Optional<ReviewReply> findByReviewIdAndDeletedAtIsNull(UUID reviewId);

    // 리뷰에 이미 삭제되지 않은 답글이 존재하는지 확인
    boolean existsByReviewIdAndDeletedAtIsNull(UUID reviewId);

    Optional<ReviewReply> findByIdAndDeletedAtIsNull(UUID replyId);
}