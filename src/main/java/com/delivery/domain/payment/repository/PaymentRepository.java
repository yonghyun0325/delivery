package com.delivery.domain.payment.repository;

import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Page<Payment> findByUserIdAndPaymentStatus(
            Long userId, PaymentStatus paymentStatus, Pageable pageable);

    @Query(
            """
            select p
            from Payment p
            where p.orderId in (
                select o.id
                from Order o
                where o.storeId = :storeId
            )
            """)
    Page<Payment> findByStoreId(UUID storeId, Pageable pageable);

    @Query(
            """
            select p
            from Payment p
            where p.paymentStatus = :paymentStatus
              and p.orderId in (
                select o.id
                from Order o
                where o.storeId = :storeId
            )
            """)
    Page<Payment> findByStoreIdAndPaymentStatus(
            UUID storeId, PaymentStatus paymentStatus, Pageable pageable);
}
