package com.delivery.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.config.AbstractControllerTest;
import com.delivery.config.WithMockCustomUser;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.service.UserAdminService;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAdminControllerUnitTest extends AbstractControllerTest {

    @MockitoBean private UserAdminService userAdminService;

    @Test
    @WithMockCustomUser(id = 2L, role = "MASTER")
    @DisplayName("유저 조회 성공 시 200과 유저 정보 반환")
    void findUserInfo_success() throws Exception {
        // given
        UserAdminResponse response =
                new UserAdminResponse(
                        1L,
                        "test1234",
                        "닉네임",
                        "01012345678",
                        UserStatus.ACTIVE,
                        Set.of(Role.CUSTOMER),
                        LocalDateTime.now(),
                        LocalDateTime.now());
        // given
        given(userAdminService.findUserInfo(1L)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/admin/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(MockMvcResultHandlers.print());
        ;

        verify(userAdminService).findUserInfo(1L);
    }

    //    @Test
    //    @DisplayName("권한이 없을 시 유저 조회가 실패한다.")
    //    @WithAnonymousUser
    //    void findUserInfo_fail_when_role() throws Exception {
    //        // given
    //        given(userAdminService.findUserInfo(1L))
    //                .willThrow(new AccessDeniedException("123"));
    //
    //        // when & then
    //        mockMvc.perform(
    //                        get("/api/v1/admin/users/{userId}", 1L)
    //                                .contentType(MediaType.APPLICATION_JSON))
    //                .andExpect(status().isForbidden());
    //
    //        verify(userAdminService).findUserInfo(1L);
    //    }

    @Test
    void getUserInfo() {}

    @Test
    void getAllUserInfo() {}

    @Test
    void updateUserRole() {}
}
