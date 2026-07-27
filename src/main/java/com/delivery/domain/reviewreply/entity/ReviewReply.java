package com.delivery.domain.reviewreply.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.domain.review.entity.Review;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_review_reply")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReply extends BaseEntity {

    // 답글 상태값을 상수로 관리하여 오타와 중복을 방지
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DELETED = "DELETED";

    // 사장님 답글 ID
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_reply_id")
    private UUID id;

    // 리뷰와 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    // 사장님 답글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 답글 작성한 사장님 ID
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // 답글 상태
    @Column(nullable = false, length = 20)
    private String status;

    // 답글 생성
    public ReviewReply(Review review, String content, Long ownerId) {
        this.review = review;
        this.content = content;
        this.ownerId = ownerId;
        this.status = STATUS_ACTIVE;
    }

    // 답글 수정
    public void update(String content) {
        this.content = content;
    }

    // 답글 삭제 처리
    public void delete(String deletedBy) {
        this.status = STATUS_DELETED;
        super.delete(deletedBy);
    }
}
