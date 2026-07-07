package com.delivery.domain.order.entity;

import com.delivery.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "p_order",
        indexes = {
                // 고객 본인 주문 내역 확인용
                @Index(name = "idx_order_user_created_at", columnList = "user_id, created_at"),
                // 가게 주문 내역 조회(상태별)
                @Index(name = "idx_order_store_status_created_at", columnList = "store_id, status, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(Long userId, UUID storeId, String deliveryAddress, String createdBy) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.storeId = storeId;
        this.status = OrderStatus.REQUESTED;
        this.deliveryAddress = deliveryAddress;
        this.totalPrice = 0;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        this.totalPrice += orderItem.getSubtotalPrice();
    }

    public void changeStatus(OrderStatus status, String updatedBy) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;

        if (status == OrderStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }

        if (status == OrderStatus.CUSTOMER_CANCELLED || status == OrderStatus.REJECTED) {
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public void delete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}