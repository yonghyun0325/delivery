package com.delivery.domain.payment.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaymentPageResponse(
        List<PaymentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static PaymentPageResponse from(Page<PaymentResponse> page) {
        return new PaymentPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
