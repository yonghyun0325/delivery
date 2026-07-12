package com.delivery.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "배송지 등록 요청")
public record CreateAddressRequest(
        @NotBlank(message = "REQUIRED_VALUE")
                @Schema(description = "주소", example = "서울 강남구 테헤란로 311")
                String address,
        @NotBlank(message = "REQUIRED_VALUE")
                @Schema(description = "상세 주소", example = "(역삼동, 아남타워빌딩) 3층")
                String addressDetail,
        @NotNull(message = "REQUIRED_VALUE") @Schema(description = "기본 배송지", example = "true")
                Boolean isDefault) {}
