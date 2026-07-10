package com.delivery.domain.reviewreply.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReviewReplyRequest {

    // 사장님 답글 내용
    @NotBlank(message = "답글 내용은 필수입니다.")
    @Size(max = 1000, message = "답글은 최대 1000자까지 입력할 수 있습니다.")
    private String content;
}