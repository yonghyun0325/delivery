// package com.delivery.domain.review.service;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import com.delivery.domain.review.dto.request.ReviewRequest;
// import com.delivery.domain.review.dto.response.ReviewResponse;
// import com.delivery.domain.review.repository.ReviewRepository;
// import java.util.UUID;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;
//
// @Transactional
// @SpringBootTest
// class ReviewServiceTest {
//
//    @Autowired private ReviewService reviewService;
//
//    @Autowired private ReviewRepository reviewRepository;
//
//    @Test
//    @DisplayName("리뷰 등록 성공")
//    void createReview_success() {
//        // given
//        ReviewRequest request =
//                new ReviewRequest(
//                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5, "정말 맛있었습니다!");
//
//        // when
//        ReviewResponse response = reviewService.createReview(request);
//
//        // then
//        assertThat(response.getReviewId()).isNotNull();
//        assertThat(response.getRating()).isEqualTo(5);
//        assertThat(response.getContent()).isEqualTo("정말 맛있었습니다!");
//        assertThat(reviewRepository.count()).isEqualTo(1);
//    }
// }
