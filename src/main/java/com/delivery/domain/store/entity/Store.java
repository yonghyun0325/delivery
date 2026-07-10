package com.delivery.domain.store.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.domain.store.dto.request.StoreRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

    @Builder.Default
    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false;

    @Builder.Default
    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;

    public void update(StoreRequest request) {
        this.categoryId = request.categoryId();
        this.regionId = request.regionId();
        this.name = request.name();
        this.address = request.address();
        this.phone = request.phone();
        this.description = request.description();
        this.minOrderAmount = request.minOrderAmount();
    }

    public void updateStatus(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void updateAverageRating(Double averageRating) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
    }
}