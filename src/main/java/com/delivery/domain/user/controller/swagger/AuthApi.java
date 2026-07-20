package com.delivery.domain.user.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "회원")
public interface AuthApi {
    @Operation(summary = "회원가입", description = "사용자가 회원가입 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 유효성 체크 실패."),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<String>> signUp(
            @Valid @RequestBody SignUpRequest request);

    @Operation(summary = "로그인", description = "사용자가 로그인 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 아이디, 비밀번호 틀림."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<String>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "로그아웃", description = "사용자가 로그아웃 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃에 성공"),
        @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<Void>> logout(HttpServletRequest request);

    @Operation(summary = "리프래시 토큰 발급", description = "사용자가 리프래시 토큰을 사용해 토큰을 재발급 받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재발급에 성공"),
        @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<String>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken);
}
