package com.delivery.domain.payment.service;

import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentStatus;
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
    private final StoreRepository storeRepository;

    public PaymentResponse getPayment(UUID paymentId, CustomUserDetails userDetail) {
        Payment payment = getPaymentOrThrow(paymentId);
        validatePaymentAccess(payment, userDetail);
        return PaymentResponse.from(payment);
    }

    public PaymentPageResponse getMyPayments(
            CustomUserDetails userDetail, int page, int size, PaymentStatus status) {
        validatePageRequest(page, size);

        Pageable pageable = createPageable(page, size);
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

        Pageable pageable = PageRequest.of(page, size);
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

        if (payment.isCanceled()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }

        payment.cancel(cancelReason);
        return PaymentResponse.from(payment);
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paidAt"));
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

        throw new BusinessException(GlobalErrorCode.FORBIDDEN);
    }

    private void validateStoreAccess(UUID storeId, CustomUserDetails userDetail) {
        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (hasRole(userDetail, Role.OWNER)) {
            validateStoreOwnership(storeId, userDetail.getId());
            return;
        }

        throw new BusinessException(GlobalErrorCode.FORBIDDEN);
    }

    private void validateCancelAccess(Payment payment, CustomUserDetails userDetail) {
        if (hasAnyRole(userDetail, Role.MANAGER, Role.MASTER)) {
            return;
        }

        if (hasRole(userDetail, Role.OWNER)) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN);
        }

        if (hasRole(userDetail, Role.CUSTOMER) && payment.isOwnedBy(userDetail.getId())) {
            return;
        }

        throw new BusinessException(GlobalErrorCode.FORBIDDEN);
    }

    private void validateStoreOwnership(UUID storeId, Long userId) {
        Store store =
                storeRepository
                        .findByStoreIdAndDeletedAtIsNull(storeId)
                        .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));

        if (!store.getUserId().equals(userId)) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN);
        }
    }

    private UUID getStoreIdByPaymentId(UUID paymentId) {
        return paymentRepository
                .findStoreIdByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(GlobalErrorCode.INVALID_PAGE_REQUEST);
        }
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
