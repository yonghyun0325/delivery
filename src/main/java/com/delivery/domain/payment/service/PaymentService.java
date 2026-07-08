package com.delivery.domain.payment.service;

import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.repository.PaymentRepository;
import com.delivery.domain.user.entity.Role;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.security.config.CustomUserDetail;
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

    public PaymentResponse getPayment(UUID paymentId, CustomUserDetail userDetail) {
        Payment payment = getPaymentOrThrow(paymentId);

        if (hasRole(userDetail, Role.CUSTOMER) && !payment.isOwnedBy(userDetail.getId())) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN);
        }

        return PaymentResponse.from(payment);
    }

    public PaymentPageResponse getMyPayments(
            CustomUserDetail userDetail, int page, int size, PaymentStatus status) {
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
            UUID storeId, int page, int size, PaymentStatus status) {
        validatePageRequest(page, size);

        Pageable pageable = createPageable(page, size);
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
            UUID paymentId, String cancelReason, CustomUserDetail userDetail) {
        Payment payment = getPaymentOrThrow(paymentId);

        if (hasRole(userDetail, Role.CUSTOMER) && !payment.isOwnedBy(userDetail.getId())) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN);
        }

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

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(GlobalErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    private boolean hasRole(CustomUserDetail userDetail, Role role) {
        return userDetail.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role.getAuthority()));
    }
}
