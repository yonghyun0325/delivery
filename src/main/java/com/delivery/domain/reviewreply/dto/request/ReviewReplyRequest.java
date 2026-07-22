package com.delivery.domain.reviewreply.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewReplyRequest(

        // 답글 내용은 필수이며 최대 1000자까지 허용
        @NotBlank(message = "EMPTY_REPLY_CONTENT")
        @Size(max = 1000, message = "REVIEW_REPLY_CONTENT_TOO_LONG")
        String content) {
}