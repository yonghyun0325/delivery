package com.delivery.domain.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.service.UserService;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.testconfig.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired private MockMvc mockMvc; // ◀ 필드 주입으로 변경
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtRequestFilter jwtRequestFilter;
    @MockitoBean ErrorCodeRegistry errorCodeRegistry;

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("유저 정보 조회 성공 시 200과 유저 정보를 반환한다.")
    void getUserInfo_Success() throws Exception {
        // given
        CustomUserDetails userDetails =
                (CustomUserDetails)
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserResponse response =
                new UserResponse(
                        userDetails.getUsername(),
                        userDetails.getNickName(),
                        userDetails.getPhoneNumber(),
                        Set.of(Role.CUSTOMER));
        given(userService.findUserInfo(eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    //    @Test
    //    void updateUser_success() throws Exception {
    //        // given
    //        UpdateUserRequest updateUserRequest =
    //                new UpdateUserRequest("nickname")
    //        given(.(eq(), eq())).willReturn();
    //
    //        // when & then
    //        mockMvc.perform(
    //                    patch("/api/v1/users/me")
    //                            .contentType(MediaType.APPLICATION_JSON)
    //                            .content(objectMapper.writeValueAsString()))
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$.success").value(true))
    //                .andExpect(jsonPath("$.data.").value());
    //    }

    @Test
    void deleteUser() {}
}
