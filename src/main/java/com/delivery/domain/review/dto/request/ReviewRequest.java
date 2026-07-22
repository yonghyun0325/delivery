package com.delivery.domain.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    @Size(max = 1000, message = "REVIEW_CONTENT_TOO_LONG")
    private String content;
}
