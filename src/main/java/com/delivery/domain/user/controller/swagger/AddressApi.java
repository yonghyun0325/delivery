package com.delivery.domain.user.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "배송지", description = "회원 배송지 관련 API")
public interface AddressApi {

    @Operation(summary = "배송지 생성", description = "회원이 배송지를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "배송지 생성에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 배송지 개수 초과(최대 10개)."),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateAddressRequest request);

    @Operation(summary = "배송지 목록 조회", description = "회원이 자신의 배송지 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배송지 목록 조회에 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<List<AddressResponse>>> getAddressList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "배송지 조회", description = "회원이 자신의 배송지를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배송지 조회에 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 배송지입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<AddressResponse>> getAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId);

    @Operation(summary = "배송지 수정", description = "회원이 배송지를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배송지 수정에 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 배송지입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request);

    @Operation(summary = "배송지 삭제", description = "회원이 배송지를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배송지 삭제에 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 배송지입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId);
}
