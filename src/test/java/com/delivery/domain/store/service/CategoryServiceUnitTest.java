package com.delivery.domain.store.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.domain.store.dto.request.CategoryRequest;
import com.delivery.domain.store.entity.Category;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.repository.CategoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Nested
    @DisplayName("카테고리 등록 실패 테스트")
    class CreateCategory {

        @Test
        @DisplayName("중복된 카테고리이면 예외가 발생해야 한다.")
        void createCategory_fail_when_duplicate() {
            CategoryRequest request = new CategoryRequest("한식");
            when(categoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("이미 등록된 카테고리입니다.");

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("카테고리 수정 실패 테스트")
    class UpdateCategory {

        @Test
        @DisplayName("존재하지 않는 카테고리 수정 시 예외가 발생해야 한다.")
        void updateCategory_fail_when_not_found() {
            UUID categoryId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest("중식");
            when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("카테고리를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 이름으로 수정 시 예외가 발생해야 한다.")
        void updateCategory_fail_when_duplicate_name() {
            UUID categoryId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest("중식");
            Category category = Category.builder().name("한식").build();

            when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId))
                    .thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndDeletedAtIsNull("중식")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("이미 등록된 카테고리입니다.");
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 실패 테스트")
    class DeleteCategory {

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제 시 예외가 발생해야 한다.")
        void deleteCategory_fail_when_not_found() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, "admin"))
                    .isInstanceOf(StoreException.class)
                    .hasMessage("카테고리를 찾을 수 없습니다.");
        }
    }
}