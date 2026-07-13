package com.delivery.domain.user.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> data, int totalPages, int pageSize, long totalElements, int pageNumber) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                page.getNumber());
    }
}
