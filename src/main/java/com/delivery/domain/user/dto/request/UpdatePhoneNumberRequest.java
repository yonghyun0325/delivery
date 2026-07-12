package com.delivery.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "휴대폰 번호 수정 요청")
public record UpdatePhoneNumberRequest(
        @Size(min = 11, max = 11, message = "INVALID_PHONE_NUMBER")
                @Pattern(regexp = "^010\\d+$", message = "INVALID_PHONE_NUMBER")
                String phoneNumber) {}
