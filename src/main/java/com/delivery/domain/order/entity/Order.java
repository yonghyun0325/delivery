package com.delivery.domain.order.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.common.util.CryptoConverter;
import com.delivery.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "p_order",
        indexes = {
            // 고객 본인 주문 내역 확인용
            @Index(name = "idx_order_user_created_at", columnList = "user_id, created_at"),
            // 가게 주문 내역 조회(상태별)
            @Index(
                    name = "idx_order_store_status_created_at",
                    columnList = "store_id, status, created_at")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(Long userId, UUID storeId, String deliveryAddress) {
        this.userId = userId;
        this.storeId = storeId;
        this.status = OrderStatus.REQUESTED;
        this.deliveryAddress = deliveryAddress;
        this.totalPrice = 0;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        this.totalPrice += orderItem.getSubtotalPrice();
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;

        if (status == OrderStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }

        if (status == OrderStatus.CUSTOMER_CANCELLED || status == OrderStatus.REJECTED) {
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (this.status == OrderStatus.COMPLETED
                || this.status == OrderStatus.REJECTED
                || this.status == OrderStatus.CUSTOMER_CANCELLED) {
            return false;
        }

        return switch (this.status) {
            case REQUESTED ->
                    nextStatus == OrderStatus.ACCEPTED
                            || nextStatus == OrderStatus.REJECTED
                            || nextStatus == OrderStatus.CUSTOMER_CANCELLED;
            case ACCEPTED -> nextStatus == OrderStatus.COOKING;
            case COOKING -> nextStatus == OrderStatus.DELIVERING;
            case DELIVERING -> nextStatus == OrderStatus.DELIVERED;
            case DELIVERED -> nextStatus == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    public boolean isCancelableByCustomerAtNow() {
        return !LocalDateTime.now().isAfter(this.getCreatedAt().plusMinutes(5));
    }
}
