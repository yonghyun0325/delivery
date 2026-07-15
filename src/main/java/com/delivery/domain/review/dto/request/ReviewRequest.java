package com.delivery.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    @NotNull(message = "ORDER_ID_REQUIRED")
    private UUID orderId;

    @NotNull(message = "RATING_REQUIRED")
    @Min(value = 1, message = "INVALID_RATING")
    @Max(value = 5, message = "INVALID_RATING")
    private Integer rating;

    @NotBlank(message = "EMPTY_CONTENT")
    private String content;
}