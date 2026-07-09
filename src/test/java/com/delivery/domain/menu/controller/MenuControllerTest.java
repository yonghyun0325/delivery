package com.delivery.domain.menu.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.domain.menu.dto.request.CreateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuVisibilityRequest;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MenuService menuService;

    // @WebMvcTest는 Filter 타입 빈을 자동으로 스캔 대상에 포함시키는데,
    // JwtRequestFilter는 JwtUtil에 의존해 실제 컨텍스트 로딩이 실패한다. 모킹으로 우회.
    @MockitoBean private JwtRequestFilter jwtRequestFilter;

    @Nested
    @DisplayName("메뉴 생성")
    class CreateMenu {

        @Test
        @DisplayName("생성에 성공하면 201과 생성된 메뉴를 반환한다")
        void createMenu_returns201() throws Exception {
            MenuResponse response = MenuResponse.from(new MenuEntity(STORE_ID, "김치찌개", "설명", 8000));
            given(
                            menuService.createMenu(
                                    eq(STORE_ID),
                                    eq("김치찌개"),
                                    eq("설명"),
                                    eq(8000),
                                    eq(false),
                                    isNull()))
                    .willReturn(response);

            CreateMenuRequest request = new CreateMenuRequest("김치찌개", "설명", 8000, false, null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.data.name").value("김치찌개"))
                    .andExpect(jsonPath("$.data.price").value(8000))
                    .andExpect(jsonPath("$.data.hidden").value(false));
        }

        @Test
        @DisplayName("이름이 비어있으면 400과 INVALID_MENU_NAME 에러를 반환한다")
        void createMenu_returns400_whenNameBlank() throws Exception {
            CreateMenuRequest request = new CreateMenuRequest("", "설명", 8000, false, null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("INVALID_MENU_NAME"));
        }

        @Test
        @DisplayName("가격이 0 이하이면 400과 INVALID_MENU_PRICE 에러를 반환한다")
        void createMenu_returns400_whenPriceNotPositive() throws Exception {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", "설명", 0, false, null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("INVALID_MENU_PRICE"));
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 목록 조회")
    class GetStoreMenus {

        @Test
        @DisplayName("메뉴 목록을 200과 함께 반환한다")
        void getStoreMenus_returns200() throws Exception {
            MenuResponse response = MenuResponse.from(new MenuEntity(STORE_ID, "김치찌개", "설명", 8000));
            given(menuService.getStoreMenus(STORE_ID)).willReturn(List.of(response));

            mockMvc.perform(get("/api/v1/stores/{storeId}/menus", STORE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("김치찌개"));
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetMenu {

        @Test
        @DisplayName("존재하면 200과 메뉴 정보를 반환한다")
        void getMenu_returns200_whenExists() throws Exception {
            UUID menuId = UUID.randomUUID();
            MenuResponse response = MenuResponse.from(new MenuEntity(STORE_ID, "김치찌개", "설명", 8000));
            given(menuService.getMenu(menuId)).willReturn(response);

            mockMvc.perform(get("/api/v1/menus/{menuId}", menuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("김치찌개"));
        }

        @Test
        @DisplayName("존재하지 않으면 404와 MENU_NOT_FOUND 에러를 반환한다")
        void getMenu_returns404_whenNotFound() throws Exception {
            UUID menuId = UUID.randomUUID();
            given(menuService.getMenu(menuId))
                    .willThrow(new MenuException(MenuErrorCode.MENU_NOT_FOUND));

            mockMvc.perform(get("/api/v1/menus/{menuId}", menuId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("MENU_NOT_FOUND"))
                    .andExpect(
                            jsonPath("$.message").value(MenuErrorCode.MENU_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class UpdateMenu {

        @Test
        @DisplayName("수정에 성공하면 200과 수정된 메뉴를 반환한다")
        void updateMenu_returns200() throws Exception {
            UUID menuId = UUID.randomUUID();
            MenuResponse response =
                    MenuResponse.from(new MenuEntity(STORE_ID, "된장찌개", "새 설명", 9000));
            given(menuService.updateMenu(eq(menuId), eq("된장찌개"), eq("새 설명"), eq(9000)))
                    .willReturn(response);

            UpdateMenuRequest request = new UpdateMenuRequest("된장찌개", "새 설명", 9000);

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("된장찌개"))
                    .andExpect(jsonPath("$.data.price").value(9000));
        }

        @Test
        @DisplayName("이름이 비어있으면 400과 INVALID_MENU_NAME 에러를 반환한다")
        void updateMenu_returns400_whenNameBlank() throws Exception {
            UUID menuId = UUID.randomUUID();
            UpdateMenuRequest request = new UpdateMenuRequest("", "새 설명", 9000);

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_MENU_NAME"));
        }

        @Test
        @DisplayName("가격이 0 이하이면 400과 INVALID_MENU_PRICE 에러를 반환한다")
        void updateMenu_returns400_whenPriceNotPositive() throws Exception {
            UUID menuId = UUID.randomUUID();
            UpdateMenuRequest request = new UpdateMenuRequest("된장찌개", "새 설명", 0);

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_MENU_PRICE"));
        }
    }

    @Nested
    @DisplayName("숨김 상태 변경")
    class UpdateVisibility {

        @Test
        @DisplayName("변경에 성공하면 200과 변경된 메뉴를 반환한다")
        void updateVisibility_returns200() throws Exception {
            UUID menuId = UUID.randomUUID();
            MenuEntity menu = new MenuEntity(STORE_ID, "김치찌개", "설명", 8000);
            menu.updateHidden(true);
            given(menuService.updateVisibility(menuId, true)).willReturn(MenuResponse.from(menu));

            UpdateMenuVisibilityRequest request = new UpdateMenuVisibilityRequest(true);

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}/visibility", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.hidden").value(true));
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class DeleteMenu {

        @Test
        @DisplayName("삭제에 성공하면 204가 아닌 200과 data null을 반환한다")
        void deleteMenu_returns200WithNullData() throws Exception {
            UUID menuId = UUID.randomUUID();

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_OWNER"));
            CustomUserDetails principal =
                    CustomUserDetails.builder()
                            .id(1L)
                            .username("owner1")
                            .authorities(authorities)
                            .build();
            SecurityContextHolder.getContext()
                    .setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, null, authorities));
            try {
                mockMvc.perform(delete("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(nullValue()));
                verify(menuService).deleteMenu(eq(menuId), eq("1_owner1"));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
