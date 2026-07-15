package com.delivery.domain.reviewreply.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReviewReplyRequest(

        @NotBlank(message = "EMPTY_REPLY_CONTENT")
        String content) {
}