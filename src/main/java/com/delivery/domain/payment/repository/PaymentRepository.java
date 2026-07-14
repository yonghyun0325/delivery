package com.delivery.domain.payment.repository;

import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByOrderId(UUID orderId);

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
                  and o.deletedAt is null
            )
            """)
    Page<Payment> findByStoreId(UUID storeId, Pageable pageable);

    @Query(
            """
            select p
            from Payment p
            where p.orderId in (
                select o.id
                from Order o
                where o.storeId = :storeId
                  and o.deletedAt is null
            )
              and p.paymentStatus = :paymentStatus
            """)
    Page<Payment> findByStoreIdAndPaymentStatus(
            UUID storeId, PaymentStatus paymentStatus, Pageable pageable);

    @Query(
            value =
                    """
                    select o.store_id
                    from p_payment p
                    join p_order o on p.order_id = o.id
                    where p.payment_id = :paymentId
                    """,
            nativeQuery = true)
    Optional<UUID> findStoreIdByPaymentId(UUID paymentId);
}
