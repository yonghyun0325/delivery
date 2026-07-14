package com.delivery.domain.order.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.response.OrderCreateResponse;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.dto.response.OrderListResponse;
import com.delivery.domain.order.dto.response.OrderStatusResponse;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Order",
        description =
                """
                주문 생성, 조회, 상태 변경 및 삭제 API입니다.

                주문 상태 흐름:
                REQUESTED(고객 주문 요청)
                → ACCEPTED → COOKING → DELIVERING
                → DELIVERED
                → COMPLETED(고객 주문 최종 완료)

                종료 상태:
                REJECTED, CUSTOMER_CANCELLED
                """)
public interface OrderControllerDocs {

    @Operation(
            summary = "고객 주문 생성",
            description =
                    """
                    CUSTOMER 권한 사용자가 주문을 생성합니다.

                    주문 생성 시 다음 항목을 검증합니다.
                    - 가게 존재 여부 및 영업 상태
                    - 메뉴 존재 여부
                    - 메뉴의 가게 소속 여부
                    - 메뉴 숨김 여부
                    - 메뉴 가격
                    - 최소 주문 금액

                    메뉴명과 가격은 주문 생성 당시 값으로 저장됩니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
        @ApiResponse(
                responseCode = "400",
                description =
                        """
                        잘못된 주문 수량,
                        최소 주문 금액 미충족,
                        영업 중이 아닌 가게
                        """),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "CUSTOMER 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 메뉴를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderCreateResponse>> createOrder(
            @Parameter(description = "주문 생성 요청 정보", required = true) OrderCreateRequest request,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "주문 단건 조회",
            description =
                    """
                    주문 ID로 주문 상세 정보를 조회합니다.

                    접근 범위:
                    - CUSTOMER: 본인이 생성한 주문
                    - OWNER: 본인이 소유한 가게의 주문
                    - MANAGER, MASTER: 전체 주문
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "주문 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderDetailResponse>> getOrder(
            @Parameter(
                            description = "조회할 주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "내 주문 내역 조회",
            description =
                    """
                    CUSTOMER 권한 사용자가 본인의 주문 내역을 조회합니다.

                    조회 조건:
                    - 시작일
                    - 종료일
                    - 주문 상태
                    - 페이지 번호
                    - 페이지 크기
                    - 정렬 조건

                    페이지 크기는 10, 30, 50을 지원하며,
                    그 외 값은 10으로 보정됩니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 내역 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 또는 조회 조건"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "CUSTOMER 권한 없음")
    })
    ResponseEntity<RestApiResponse<OrderListResponse>> getMyOrders(
            @Parameter(description = "조회 시작일", example = "2026-07-01") LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-07-31") LocalDate endDate,
            @Parameter(description = "주문 상태", schema = @Schema(implementation = OrderStatus.class))
                    OrderStatus status,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기: 10, 30, 50", example = "10") int size,
            @Parameter(description = "정렬 조건", example = "createdAt,desc") String sort,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "가게 주문 내역 조회",
            description =
                    """
                    OWNER, MANAGER, MASTER 권한 사용자가
                    특정 가게의 주문 내역을 조회합니다.

                    접근 범위:
                    - OWNER: 본인이 소유한 가게만 조회 가능
                    - MANAGER, MASTER: 모든 가게 조회 가능

                    날짜, 주문 상태, 페이지 및 정렬 조건을 선택적으로 적용할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가게 주문 내역 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 또는 조회 조건"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderListResponse>> getStoreOrders(
            @Parameter(
                            description = "조회할 가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(description = "조회 시작일", example = "2026-07-01") LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-07-31") LocalDate endDate,
            @Parameter(description = "주문 상태", schema = @Schema(implementation = OrderStatus.class))
                    OrderStatus status,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기: 10, 30, 50", example = "10") int size,
            @Parameter(description = "정렬 조건", example = "createdAt,desc") String sort,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "관리자 주문 삭제",
            description =
                    """
                    MANAGER 또는 MASTER 권한 사용자가 주문을 Soft Delete 처리합니다.

                    주문 데이터는 실제 삭제되지 않으며,
                    삭제 시각과 삭제 사용자 정보가 기록됩니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<Void>> deleteOrder(
            @Parameter(
                            description = "삭제할 주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "고객 주문 취소",
            description =
                    """
                    CUSTOMER 권한 사용자가 본인의 주문을 취소합니다.

                    취소 가능 조건:
                    - 현재 상태가 REQUESTED
                    - 주문 생성 후 5분 이내
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 취소 성공"),
        @ApiResponse(responseCode = "400", description = "취소할 수 없는 주문 상태 또는 취소 시간 초과"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "본인의 주문이 아님"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> cancelOrder(
            @Parameter(
                            description = "취소할 주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "가게 주문 거절",
            description =
                    """
                    REQUESTED 상태의 주문을 REJECTED 상태로 변경합니다.

                    OWNER는 본인 가게 주문만 처리할 수 있으며,
                    MANAGER와 MASTER는 모든 가게 주문을 처리할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 거절 성공"),
        @ApiResponse(responseCode = "400", description = "허용되지 않은 주문 상태 변경"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> rejectOrder(
            @Parameter(
                            description = "가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(
                            description = "주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440001")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "가게 주문 수락",
            description =
                    """
                    REQUESTED 상태의 주문을 ACCEPTED 상태로 변경합니다.

                    OWNER는 본인 가게 주문만 처리할 수 있으며,
                    MANAGER와 MASTER는 모든 가게 주문을 처리할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 수락 성공"),
        @ApiResponse(responseCode = "400", description = "허용되지 않은 주문 상태 변경"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> acceptOrder(
            @Parameter(
                            description = "가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(
                            description = "주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440001")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "주문 조리 시작",
            description =
                    """
                    ACCEPTED 상태의 주문을 COOKING 상태로 변경합니다.

                    OWNER는 본인 가게 주문만 처리할 수 있으며,
                    MANAGER와 MASTER는 모든 가게 주문을 처리할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조리 중 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "허용되지 않은 주문 상태 변경"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> startCooking(
            @Parameter(
                            description = "가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(
                            description = "주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440001")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "주문 배달 시작",
            description =
                    """
                    COOKING 상태의 주문을 DELIVERING 상태로 변경합니다.

                    OWNER는 본인 가게 주문만 처리할 수 있으며,
                    MANAGER와 MASTER는 모든 가게 주문을 처리할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달 중 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "허용되지 않은 주문 상태 변경"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> startDelivery(
            @Parameter(
                            description = "가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(
                            description = "주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440001")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "주문 배달 완료",
            description =
                    """
                    DELIVERING 상태의 주문을 DELIVERED 상태로 변경합니다.

                    OWNER는 본인 가게 주문만 처리할 수 있으며,
                    MANAGER와 MASTER는 모든 가게 주문을 처리할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달 완료 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "허용되지 않은 주문 상태 변경"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "가게 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "가게 또는 주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> completeDelivery(
            @Parameter(
                            description = "가게 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID storeId,
            @Parameter(
                            description = "주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440001")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);

    @Operation(
            summary = "고객 주문 최종 완료",
            description =
                    """
                    CUSTOMER 권한 사용자가 배달 완료된 본인의 주문을
                    최종 완료 상태로 변경합니다.

                    DELIVERED 상태의 주문만 COMPLETED 상태로 변경할 수 있습니다.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 최종 완료 성공"),
        @ApiResponse(responseCode = "400", description = "완료할 수 없는 주문 상태"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "본인의 주문이 아님"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<OrderStatusResponse>> completeOrder(
            @Parameter(
                            description = "최종 완료할 주문 ID",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000")
                    UUID orderId,
            @Parameter(hidden = true) CustomUserDetails userDetails);
}
