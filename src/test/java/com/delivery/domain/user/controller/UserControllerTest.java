package com.delivery.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.config.WithMockCustomUser;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.service.UserService;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtRequestFilter jwtRequestFilter;
    @MockitoBean ErrorCodeRegistry errorCodeRegistry;

    @Test
    @WithMockCustomUser
    @DisplayName("유저 정보 조회 성공 시 200과 유저 정보를 반환한다.")
    void getUserInfo_Success() throws Exception {
        // given
        UserResponse response =
                new UserResponse("test1234", "test!", "01012345678", Set.of(Role.CUSTOMER));
        given(userService.findUserInfo(eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(response.username()))
                .andExpect(jsonPath("$.data.nickName").value(response.nickName()))
                .andExpect(jsonPath("$.data.phoneNumber").value(response.phoneNumber()))
                .andDo(MockMvcResultHandlers.print());

        verify(userService).findUserInfo(eq(1L));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("닉네임 업데이트 성공 시 200과 유저 정보를 반환한다.")
    void updateNickName_success() throws Exception {
        // given
        UpdateNickNameRequest request = new UpdateNickNameRequest("닉네임2");

        UserResponse response =
                new UserResponse(
                        "test1234", request.nickName(), "01012345678", Set.of(Role.CUSTOMER));

        given(userService.updateNickName(eq(1L), any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/users/me/nickname")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickName").value("닉네임2"))
                .andDo(MockMvcResultHandlers.print());

        verify(userService).updateNickName(eq(1L), any());
    }

    @WithMockCustomUser
    @ParameterizedTest
    @MethodSource("nickNameTestCase")
    @DisplayName("닉네임 변경 시 유효성 체크에 실패하면 400 예외를 반환한다.")
    void updateNickName_fail_when_invalid(UpdateNickNameRequest request) throws Exception {
        // when & then
        mockMvc.perform(
                        patch("/api/v1/users/me/nickname")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        verifyNoInteractions(userService);
    }

    static Stream<UpdateNickNameRequest> nickNameTestCase() {
        return Stream.of(
                new UpdateNickNameRequest("안"),
                new UpdateNickNameRequest("@!#@!$!@$!@"),
                new UpdateNickNameRequest("ㅁㄴㅇㅁㄴㅇㄴㅁㄹㄴㅁㅇㄴㅁㅇㅁㄴㅇㅁㄴㄹㄴㅁㅇㅁㄴㅇㄴㅁ"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("연락처 변경 성공 시 200과 유저 정보를 반환한다.")
    void updatePhoneNumber_success() throws Exception {
        // given
        UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest("01012345678");

        UserResponse response =
                new UserResponse(
                        "test1234", request.phoneNumber(), "01012345678", Set.of(Role.CUSTOMER));

        given(userService.updateNickName(eq(1L), any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/users/me/phone-number")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(userService).updatePhoneNumber(eq(1L), any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("연락처 변경 시 유효성 체크에 실패하면 400 예외를 반환한다.")
    void updatePhoneNumber_fail_when_invalid() throws Exception {
        // given
        UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest("010");

        // when & then
        mockMvc.perform(
                        patch("/api/v1/users/me/phone-number")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        verifyNoInteractions(userService);
    }
}
