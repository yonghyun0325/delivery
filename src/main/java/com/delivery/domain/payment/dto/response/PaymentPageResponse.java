package com.delivery.domain.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record PaymentPageResponse(
        @Schema(description = "결제 목록")
        List<PaymentResponse> content,
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size,
        @Schema(description = "전체 데이터 수", example = "1")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "false")
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
