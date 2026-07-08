package com.delivery.domain.payment.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_payment")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isCanceled() {
        return this.paymentStatus == PaymentStatus.CANCELED;
    }

    public void cancel(String cancelReason) {
        this.paymentStatus = PaymentStatus.CANCELED;
        this.cancelReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }
}
