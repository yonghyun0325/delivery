package com.delivery.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "배송지 응답")
public record AddressResponse(
        @Schema(description = "배송지 PK", example = "UUID") UUID addressId,
        @Schema(description = "주소", example = "서울 강남구 테헤란로 311") String address,
        @Schema(description = "상세 주소", example = "(역삼동, 아남타워빌딩) 3층") String addressDetail,
        @Schema(description = "기본 배송지", example = "true") boolean isDefault) {}
