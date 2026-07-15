package com.delivery.domain.review.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.enums.ReviewSortType;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "리뷰", description = "리뷰 및 평점 관리 API")
public interface ReviewApi {

    @Operation(summary = "리뷰 등록", description = "배송 완료 또는 주문 완료 상태의 주문에 대해 리뷰를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
        @ApiResponse(responseCode = "400", description = "평점, 리뷰 내용 또는 주문 상태가 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "본인의 주문이 아님"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 해당 주문에 리뷰가 존재함")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<ReviewResponse>> createReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request);

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID를 이용하여 삭제되지 않은 리뷰의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<ReviewResponse>> getReview(
            @Parameter(description = "조회할 리뷰 ID", required = true) @PathVariable UUID reviewId);

    @Operation(summary = "리뷰 수정", description = "리뷰 작성자 본인이 평점과 리뷰 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
        @ApiResponse(responseCode = "400", description = "평점 또는 리뷰 내용이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "리뷰 작성자가 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<ReviewResponse>> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 리뷰 ID", required = true) @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request);

    @Operation(summary = "리뷰 삭제", description = "리뷰 작성자 본인이 리뷰를 소프트 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "리뷰 작성자가 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<Void>> deleteReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "삭제할 리뷰 ID", required = true) @PathVariable UUID reviewId);

    @Operation(summary = "음식점 리뷰 목록 조회", description = "음식점 ID를 기준으로 리뷰 목록을 페이징 및 정렬하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "음식점 리뷰 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "정렬 조건이 올바르지 않음")
    })
    ResponseEntity<RestApiResponse<Page<ReviewResponse>>> getReviewsByStore(
            @Parameter(description = "리뷰를 조회할 음식점 ID", required = true) @PathVariable UUID storeId,
            @Parameter(
                            description = "리뷰 정렬 기준",
                            in = ParameterIn.QUERY,
                            schema =
                                    @Schema(
                                            allowableValues = {
                                                "LATEST",
                                                "OLDEST",
                                                "RATING_HIGH",
                                                "RATING_LOW"
                                            },
                                            defaultValue = "LATEST"))
                    @RequestParam(defaultValue = "LATEST")
                    ReviewSortType sortType,
            Pageable pageable);

    @Operation(summary = "내 리뷰 목록 조회", description = "로그인한 사용자가 작성한 삭제되지 않은 리뷰 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 리뷰 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "CUSTOMER 권한이 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RestApiResponse<List<ReviewResponse>>> getMyReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "음식점 평균 평점 조회", description = "음식점 ID를 기준으로 삭제되지 않은 리뷰의 평균 평점을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "음식점 평균 평점 조회 성공")})
    ResponseEntity<RestApiResponse<Double>> getStoreRating(
            @Parameter(description = "평균 평점을 조회할 음식점 ID", required = true) @PathVariable
                    UUID storeId);
}
