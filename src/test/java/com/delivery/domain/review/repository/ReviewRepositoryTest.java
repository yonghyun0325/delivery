package com.delivery.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.delivery.domain.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReviewRepositoryTest {

    @Autowired private ReviewRepository reviewRepository;

    @Test
    @DisplayName("리뷰 저장")
    void saveReview() {
        // given
        Review review = Review.create(UUID.randomUUID(), 1L, UUID.randomUUID(), 5, "정말 맛있었습니다!");

        // when
        Review savedReview = reviewRepository.save(review);

        // then
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("정말 맛있었습니다!");
    }

    @Test
    @DisplayName("리뷰 단건 조회")
    void findReview() {
        // given
        Review review =
                Review.create(UUID.randomUUID(), 1L, UUID.randomUUID(), 4, "배송도 빠르고 맛있었습니다.");

        Review savedReview = reviewRepository.save(review);

        // when
        Optional<Review> findReview = reviewRepository.findById(savedReview.getId());

        // then
        assertThat(findReview).isPresent();
        assertThat(findReview.get().getId()).isEqualTo(savedReview.getId());
        assertThat(findReview.get().getRating()).isEqualTo(4);
        assertThat(findReview.get().getContent()).isEqualTo("배송도 빠르고 맛있었습니다.");
    }
}
