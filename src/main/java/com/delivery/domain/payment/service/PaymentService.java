package com.delivery.domain.payment.service;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.exception.PaymentErrorCode;
import com.delivery.domain.payment.exception.PaymentException;
import com.delivery.domain.payment.repository.PaymentRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.user.entity.Role;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public PaymentResponse createPayment(
            UUID orderId, Long userId, int paymentAmount, PaymentMethod paymentMethod) {
        if (paymentAmount <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        Payment payment =
                paymentRepository.save(
                        Payment.create(orderId, userId, paymentMethod, paymentAmount));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPayment(UUID paymentId, CustomUserDetails userDetail) {
        Payment payment = getPaymentOrThrow(paymentId);
        validatePaymentAccess(payment, userDetail);
        return PaymentResponse.from(payment);
    }

    public PaymentPageResponse getMyPayments(
            CustomUserDetails userDetail, int page, int size, PaymentStatus status) {
        validatePageRequest(page, size);

        Pageable pageable = createPageable(page, normalizePageSize(size));
        Page<PaymentResponse> payments =
                (status == null
                                ? paymentRepository.findByUserId(userDetail.getId(), pageable)
                                : paymentRepository.findByUserIdAndPaymentStatus(
                                        userDetail.getId(), status, pageable))
                        .map(PaymentResponse::from);

        return PaymentPageResponse.from(payments);
    }

    public PaymentPageResponse getStorePayments(
            UUID storeId, CustomUserDetails userDetail, int page, int size, PaymentStatus status) {
        validatePageRequest(page, size);
        validateStoreAccess(storeId, userDetail);

        Pageable pageable = createPageable(page, normalizePageSize(size));
        Page<PaymentResponse> payments =
                (status == null
                                ? paymentRepository.findByStoreId(storeId, pageable)
                                : paymentRepository.findByStoreIdAndPaymentStatus(
                                        storeId, status, pageable))
                        .map(PaymentResponse::from);

        return PaymentPageResponse.from(payments);
    }

    @Transactional
    public PaymentResponse cancelPayment(
            UUID paymentId, String cancelReason, CustomUserDetails userDetail) {
        Payment payment = getPaymentOrThrow(paymentId);
        validateCancelAccess(payment, userDetail);
        Order order = getOrderOrThrow(payment.getOrderId());

        if (payment.isCanceled()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        validateCancelableOrder(order, userDetail);
        payment.cancel(cancelReason);
        order.changeStatus(OrderStatus.CUSTOMER_CANCELLED);
        return PaymentResponse.from(payment);
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paidAt"));
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository
                .findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private void validateCancelableOrder(Order order, CustomUserDetails userDetail) {
        if (!order.canTransitionTo(OrderStatus.CUSTOMER_CANCELLED)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ORDER_STATE_INVALID);
        }

        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (!order.isCancelableByCustomerAtNow()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_TIME_EXPIRED);
        }
    }

    private void validatePaymentAccess(Payment payment, CustomUserDetails userDetail) {
        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (hasRole(userDetail, Role.OWNER)) {
            UUID storeId = getStoreIdByPaymentId(payment.getPaymentId());
            validateStoreOwnership(storeId, userDetail.getId());
            return;
        }

        if (hasRole(userDetail, Role.CUSTOMER) && payment.isOwnedBy(userDetail.getId())) {
            return;
        }

        throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
    }

    private void validateStoreAccess(UUID storeId, CustomUserDetails userDetail) {
        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (hasRole(userDetail, Role.OWNER)) {
            validateStoreOwnership(storeId, userDetail.getId());
            return;
        }

        throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
    }

    private void validateCancelAccess(Payment payment, CustomUserDetails userDetail) {
        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (hasRole(userDetail, Role.OWNER)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (hasRole(userDetail, Role.CUSTOMER) && payment.isOwnedBy(userDetail.getId())) {
            return;
        }

        throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
    }

    private void validateStoreOwnership(UUID storeId, Long userId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (!store.getUserId().equals(userId)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
        }
    }

    private UUID getStoreIdByPaymentId(UUID paymentId) {
        return paymentRepository
                .findStoreIdByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new BusinessException(GlobalErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    private int normalizePageSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }

        return 10;
    }

    private boolean hasRole(CustomUserDetails userDetail, Role role) {
        return userDetail.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role.getAuthority()));
    }

    private boolean hasAnyRole(CustomUserDetails userDetail, Role... roles) {
        for (Role role : roles) {
            if (hasRole(userDetail, role)) {
                return true;
            }
        }
        return false;
    }
}
