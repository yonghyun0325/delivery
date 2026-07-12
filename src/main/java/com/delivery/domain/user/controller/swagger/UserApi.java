package com.delivery.domain.user.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.dto.response.UserValidationResponse;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "회원", description = "회원 관련 API")
public interface UserApi {
    @Operation(summary = "회원 정보 조회", description = "회원이 자신의 정보를 조회 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "닉네임 수정", description = "회원이 닉네임을 수정합니다..")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 유효성 체크 실패."),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserResponse>> updateNickName(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdateNickNameRequest request);

    @Operation(summary = "전화번호 수정", description = "회원이 전화번호를 수정합니다..")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 유효성 체크 실패."),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserResponse>> updatePhoneNumber(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdatePhoneNumberRequest request);

    @Operation(summary = "회원 탈퇴", description = "회원이 탈퇴 합니다..")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴에 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "아이디 중복 체크", description = "사용자가 아이디 중복 체크를 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복체크에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserValidationResponse>> isDuplicationUsername(
            @RequestParam String username);

    @Operation(summary = "닉네임 중복 체크", description = "사용자가 닉네임 중복 체크를 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복체크에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserValidationResponse>> isDuplicationNickname(
            @RequestParam String nickName);
}
