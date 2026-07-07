package com.delivery.domain.store.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.domain.store.dto.StoreRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
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

    public void update(StoreRequestDto request) {
        this.categoryId = request.getCategoryId();
        this.regionId = request.getRegionId();
        this.name = request.getName();
        this.address = request.getAddress();
        this.phone = request.getPhone();
        this.description = request.getDescription();
        this.minOrderAmount = request.getMinOrderAmount();
    }

    public void updateStatus(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void delete(String deletedBy) {
        super.delete(deletedBy);
    }
}