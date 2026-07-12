package com.delivery.domain.user.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증/인가", description = "인증/인가 관련 API")
public interface AuthApi {
    @Operation(summary = "회원가입", description = "사용자가 회원가입 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 유효성 체크 실패."),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<AuthResponse>> signUp(
            @Valid @RequestBody SignUpRequest request);

    @Operation(summary = "로그인", description = "사용자가 로그인 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인에 성공"),
        @ApiResponse(responseCode = "400", description = "필수 입력값 누락 또는 아이디, 비밀번호 틀림."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request);
}
