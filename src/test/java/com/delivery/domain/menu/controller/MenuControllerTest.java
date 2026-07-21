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
import com.delivery.domain.menu.dto.response.PublicMenuResponse;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.menu.exception.MenuException;
import com.delivery.domain.menu.fixture.MenuFixture;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.user.entity.Role;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.principal.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

// GlobalExceptionHandler가 ErrorCodeRegistry를 생성자 주입받는데 @WebMvcTest 슬라이스엔
// 자동으로 안 실려서 명시적으로 가져옴 - 실제 Menu 검증 실패는 MenuExceptionHandler가
// 먼저 가로채므로(HIGHEST_PRECEDENCE) 이 레지스트리 값 자체는 테스트 결과에 영향 없음.
@Import(ErrorCodeRegistry.class)
@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;

    @Autowired private JsonMapper jsonMapper;

    @MockitoBean private MenuService menuService;

    // @WebMvcTest는 Filter 타입 빈을 자동으로 스캔 대상에 포함시키는데,
    // JwtRequestFilter는 JwtUtil에 의존해 실제 컨텍스트 로딩이 실패한다. 모킹으로 우회.
    // @MockitoBean private JwtRequestFilter jwtRequestFilter;

    // @AuthenticationPrincipal은 SecurityContextHolder를 직접 읽으므로(필터 체인과 무관),
    // addFilters=false 슬라이스에서도 이렇게 수동으로 세팅해야 principal이 주입된다.
    private void setAuthenticatedPrincipal(Long id, String username, String roleAuthority) {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(roleAuthority));
        CustomUserDetails principal =
                CustomUserDetails.builder()
                        .id(id)
                        .username(username)
                        .authorities(authorities)
                        .build();
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @Nested
    @DisplayName("메뉴 생성")
    class CreateMenu {

        @Test
        @DisplayName("생성에 성공하면 201과 생성된 메뉴를 반환한다")
        void createMenu_returns201() throws Exception {
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                MenuResponse response =
                        MenuResponse.from(MenuFixture.CREATE.createEntity(STORE_ID));
                given(
                                menuService.createMenu(
                                        eq(STORE_ID),
                                        eq(MenuFixture.CREATE.menuName()),
                                        eq(MenuFixture.CREATE.description()),
                                        eq(MenuFixture.CREATE.price()),
                                        eq(false),
                                        isNull(),
                                        eq(1L),
                                        eq(false)))
                        .willReturn(response);

                CreateMenuRequest request = MenuFixture.CREATE.createRequestDto();

                mockMvc.perform(
                                post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.code").value(201))
                        .andExpect(jsonPath("$.data.name").value(MenuFixture.CREATE.menuName()))
                        .andExpect(jsonPath("$.data.price").value(MenuFixture.CREATE.price()))
                        .andExpect(jsonPath("$.data.hidden").value(false));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("이름이 비어있으면 400과 INVALID_MENU_NAME 에러를 반환한다")
        void createMenu_returns400_whenNameBlank() throws Exception {
            CreateMenuRequest request =
                    new CreateMenuRequest(
                            "",
                            MenuFixture.CREATE.description(),
                            MenuFixture.CREATE.price(),
                            false,
                            null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.error").value(MenuErrorCode.INVALID_MENU_NAME.getName()));
        }

        @Test
        @DisplayName("가격이 0 이하이면 400과 INVALID_MENU_PRICE 에러를 반환한다")
        void createMenu_returns400_whenPriceNotPositive() throws Exception {
            CreateMenuRequest request =
                    new CreateMenuRequest(
                            MenuFixture.CREATE.menuName(),
                            MenuFixture.CREATE.description(),
                            0,
                            false,
                            null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.error").value(MenuErrorCode.INVALID_MENU_PRICE.getName()));
        }

        @Test
        @DisplayName("가게 소유자가 아니면 403과 NOT_MENU_STORE_OWNER 에러를 반환한다")
        void createMenu_returns403_whenNotStoreOwner() throws Exception {
            setAuthenticatedPrincipal(2L, "other", Role.Authority.OWNER);
            try {
                given(
                                menuService.createMenu(
                                        eq(STORE_ID),
                                        eq(MenuFixture.CREATE.menuName()),
                                        eq(MenuFixture.CREATE.description()),
                                        eq(MenuFixture.CREATE.price()),
                                        eq(false),
                                        isNull(),
                                        eq(2L),
                                        eq(false)))
                        .willThrow(new MenuException(MenuErrorCode.NOT_MENU_STORE_OWNER));

                CreateMenuRequest request = MenuFixture.CREATE.createRequestDto();

                mockMvc.perform(
                                post("/api/v1/stores/{storeId}/menus", STORE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden())
                        .andExpect(
                                jsonPath("$.error")
                                        .value(MenuErrorCode.NOT_MENU_STORE_OWNER.getName()));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Nested
    @DisplayName("가게별 메뉴 목록 조회")
    class GetStoreMenus {

        @Test
        @DisplayName("소유자가 조회하면 hidden 필드가 포함된 목록을 반환한다")
        void getStoreMenus_returns200_withHiddenField_whenOwner() throws Exception {
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                MenuResponse response =
                        MenuResponse.from(MenuFixture.CREATE.createEntity(STORE_ID));
                given(menuService.getStoreMenus(STORE_ID, 1L, false)).willReturn(List.of(response));

                mockMvc.perform(get("/api/v1/stores/{storeId}/menus", STORE_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.length()").value(1))
                        .andExpect(jsonPath("$.data[0].name").value(MenuFixture.CREATE.menuName()))
                        .andExpect(jsonPath("$.data[0].hidden").exists());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("손님이 조회하면 hidden 필드가 없는 공개 목록을 반환한다")
        void getStoreMenus_returns200_withoutHiddenField_whenNotOwner() throws Exception {
            setAuthenticatedPrincipal(2L, "customer1", Role.Authority.CUSTOMER);
            try {
                PublicMenuResponse response =
                        PublicMenuResponse.from(MenuFixture.CREATE.createEntity(STORE_ID));
                given(menuService.getStoreMenus(STORE_ID, 2L, false)).willReturn(List.of(response));

                mockMvc.perform(get("/api/v1/stores/{storeId}/menus", STORE_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data[0].name").value(MenuFixture.CREATE.menuName()))
                        .andExpect(jsonPath("$.data[0].hidden").doesNotExist())
                        .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetMenu {

        @Test
        @DisplayName("소유자가 조회하면 hidden 필드가 포함된 정보를 반환한다")
        void getMenu_returns200_withHiddenField_whenOwner() throws Exception {
            UUID menuId = UUID.randomUUID();
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                MenuResponse response =
                        MenuResponse.from(MenuFixture.CREATE.createEntity(STORE_ID));
                given(menuService.getMenu(menuId, 1L, false)).willReturn(response);

                mockMvc.perform(get("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.name").value(MenuFixture.CREATE.menuName()))
                        .andExpect(jsonPath("$.data.hidden").exists());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("손님이 조회하면 hidden 필드가 없는 공개 정보를 반환한다")
        void getMenu_returns200_withoutHiddenField_whenNotOwner() throws Exception {
            UUID menuId = UUID.randomUUID();
            setAuthenticatedPrincipal(2L, "customer1", Role.Authority.CUSTOMER);
            try {
                PublicMenuResponse response =
                        PublicMenuResponse.from(MenuFixture.CREATE.createEntity(STORE_ID));
                given(menuService.getMenu(menuId, 2L, false)).willReturn(response);

                mockMvc.perform(get("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.name").value(MenuFixture.CREATE.menuName()))
                        .andExpect(jsonPath("$.data.hidden").doesNotExist());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("존재하지 않으면 404와 MENU_NOT_FOUND 에러를 반환한다")
        void getMenu_returns404_whenNotFound() throws Exception {
            UUID menuId = UUID.randomUUID();
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                given(menuService.getMenu(menuId, 1L, false))
                        .willThrow(new MenuException(MenuErrorCode.MENU_NOT_FOUND));

                mockMvc.perform(get("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.error").value(MenuErrorCode.MENU_NOT_FOUND.getName()))
                        .andExpect(
                                jsonPath("$.message")
                                        .value(MenuErrorCode.MENU_NOT_FOUND.getMessage()));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class UpdateMenu {

        @Test
        @DisplayName("수정에 성공하면 200과 수정된 메뉴를 반환한다")
        void updateMenu_returns200() throws Exception {
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                UUID menuId = UUID.randomUUID();
                MenuResponse response =
                        MenuResponse.from(MenuFixture.UPDATE.createEntity(STORE_ID));
                given(
                                menuService.updateMenu(
                                        eq(menuId),
                                        eq(MenuFixture.UPDATE.menuName()),
                                        eq(MenuFixture.UPDATE.description()),
                                        eq(MenuFixture.UPDATE.price()),
                                        eq(1L),
                                        eq(false)))
                        .willReturn(response);

                UpdateMenuRequest request = MenuFixture.UPDATE.updateRequestDto();

                mockMvc.perform(
                                patch("/api/v1/menus/{menuId}", menuId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.name").value(MenuFixture.UPDATE.menuName()))
                        .andExpect(jsonPath("$.data.price").value(MenuFixture.UPDATE.price()));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("이름이 비어있으면 400과 INVALID_MENU_NAME 에러를 반환한다")
        void updateMenu_returns400_whenNameBlank() throws Exception {
            UUID menuId = UUID.randomUUID();
            UpdateMenuRequest request =
                    new UpdateMenuRequest(
                            "", MenuFixture.UPDATE.description(), MenuFixture.UPDATE.price());

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error").value(MenuErrorCode.INVALID_MENU_NAME.getName()));
        }

        @Test
        @DisplayName("가격이 0 이하이면 400과 INVALID_MENU_PRICE 에러를 반환한다")
        void updateMenu_returns400_whenPriceNotPositive() throws Exception {
            UUID menuId = UUID.randomUUID();
            UpdateMenuRequest request =
                    new UpdateMenuRequest(
                            MenuFixture.UPDATE.menuName(), MenuFixture.UPDATE.description(), 0);

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error").value(MenuErrorCode.INVALID_MENU_PRICE.getName()));
        }
    }

    @Nested
    @DisplayName("숨김 상태 변경")
    class UpdateVisibility {

        @Test
        @DisplayName("변경에 성공하면 200과 변경된 메뉴를 반환한다")
        void updateVisibility_returns200() throws Exception {
            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                UUID menuId = UUID.randomUUID();
                MenuEntity menu = MenuFixture.CREATE.createEntity(STORE_ID);
                menu.updateHidden(true);
                given(menuService.updateVisibility(menuId, true, 1L, false))
                        .willReturn(MenuResponse.from(menu));

                UpdateMenuVisibilityRequest request = new UpdateMenuVisibilityRequest(true);

                mockMvc.perform(
                                patch("/api/v1/menus/{menuId}/visibility", menuId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.hidden").value(true));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("hidden 값이 없으면 400과 MENU_HIDDEN_STATUS_REQUIRED를 반환한다")
        void updateVisibility_withoutHidden_returnsBadRequest() throws Exception {
            UUID menuId = UUID.randomUUID();

            mockMvc.perform(
                            patch("/api/v1/menus/{menuId}/visibility", menuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(MenuErrorCode.MENU_HIDDEN_STATUS_REQUIRED.getName()));
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class DeleteMenu {

        @Test
        @DisplayName("삭제에 성공하면 204가 아닌 200과 data null을 반환한다")
        void deleteMenu_returns200WithNullData() throws Exception {
            UUID menuId = UUID.randomUUID();

            setAuthenticatedPrincipal(1L, "owner1", Role.Authority.OWNER);
            try {
                mockMvc.perform(delete("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(nullValue()));
                verify(menuService).deleteMenu(eq(menuId), eq("1_owner1"), eq(1L), eq(false));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Test
        @DisplayName("MANAGER는 소유자가 아니어도 삭제할 수 있다(우회)")
        void deleteMenu_bypassesOwnership_forManager() throws Exception {
            UUID menuId = UUID.randomUUID();

            setAuthenticatedPrincipal(9L, "manager1", Role.Authority.MANAGER);
            try {
                mockMvc.perform(delete("/api/v1/menus/{menuId}", menuId))
                        .andExpect(status().isOk());
                verify(menuService).deleteMenu(eq(menuId), eq("9_manager1"), eq(9L), eq(true));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
