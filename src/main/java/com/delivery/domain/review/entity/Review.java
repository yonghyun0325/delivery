package com.delivery.domain.review.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "p_review",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_review_order_id", columnNames = "order_id")
        })
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    protected Review(UUID orderId, Long userId, UUID storeId, Integer rating, String content) {
        this.orderId = orderId;
        this.userId = userId;
        this.storeId = storeId;
        this.rating = rating;
        this.content = content;
    }

    public static Review create(
            UUID orderId, Long userId, UUID storeId, Integer rating, String content) {
        return new Review(orderId, userId, storeId, rating, content);
    }

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
