package com.delivery.domain.store.dto.response;

import com.delivery.domain.store.entity.Category;
import java.util.UUID;

public record CategoryResponse(
        UUID categoryId,
        String name
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName()
        );
    }
}