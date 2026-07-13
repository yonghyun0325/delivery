package com.delivery.domain.menu.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.menu.dto.request.CreateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuVisibilityRequest;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.dto.response.MenuSearchView;
import com.delivery.domain.menu.dto.response.MenuView;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.user.entity.Role;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메뉴", description = "메뉴 CRUD 및 AI 설명 생성 API")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 등록
    @Operation(summary = "메뉴 등록", description = "가게에 메뉴를 등록합니다. AI 설명 생성 옵션을 지원합니다.")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<MenuResponse>> createMenu(
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateMenuRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        MenuResponse response =
                menuService.createMenu(
                        storeId,
                        request.name(),
                        request.description(),
                        request.price(),
                        request.aiGeneration(),
                        request.aiPrompt(),
                        principal.getId(),
                        hasElevatedRole(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "메뉴가 등록되었습니다.", response));
    }

    // 메뉴 목록 조회
    // 가게 소유자/MANAGER/MASTER에게는 숨김 메뉴 포함 전체 필드, 그 외(손님)에게는
    // 숨김 메뉴를 뺀 공개 필드만 반환한다(MenuView 참고).
    @Operation(summary = "가게별 메뉴 목록 조회", description = "가게에 등록된 삭제되지 않은 메뉴 목록을 조회합니다.")
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<List<MenuView>>> getStoreMenus(
            @PathVariable UUID storeId, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "메뉴 목록 조회에 성공했습니다.",
                        menuService.getStoreMenus(
                                storeId, principal.getId(), hasElevatedRole(principal))));
    }

    // 메뉴 단건 조회
    // 숨김 메뉴를 볼 권한이 없으면 404로 응답한다(MenuService 참고 - 존재 여부 비노출).
    @Operation(summary = "메뉴 단건 조회", description = "메뉴 ID로 메뉴 한 건을 조회합니다.")
    @GetMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<MenuView>> getMenu(
            @PathVariable UUID menuId, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "메뉴 조회에 성공했습니다.",
                        menuService.getMenu(
                                menuId, principal.getId(), hasElevatedRole(principal))));
    }

    // 메뉴 횡단 검색 (플랫 - 특정 가게에 종속되지 않음)
    // size는 10/30/50만 허용(그 외는 10으로 보정), 기본 정렬은 생성일 내림차순
    @Operation(summary = "메뉴 검색", description = "가게 구분 없이 이름으로 메뉴를 검색합니다.")
    @GetMapping("/api/v1/menus")
    public ResponseEntity<RestApiResponse<Page<MenuSearchView>>> searchMenus(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "메뉴 검색에 성공했습니다.",
                        menuService.searchMenus(
                                name, page, size, sort, hasElevatedRole(principal))));
    }

    // 메뉴 수정
    @Operation(summary = "메뉴 수정", description = "메뉴의 이름/설명/가격을 수정합니다.")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<MenuResponse>> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        MenuResponse response =
                menuService.updateMenu(
                        menuId,
                        request.name(),
                        request.description(),
                        request.price(),
                        principal.getId(),
                        hasElevatedRole(principal));
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "메뉴가 수정되었습니다.", response));
    }

    // 숨김 상태 업데이트
    @Operation(summary = "메뉴 숨김 상태 변경", description = "메뉴를 숨김/노출 처리합니다.")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}/visibility")
    public ResponseEntity<RestApiResponse<MenuResponse>> updateMenuVisibility(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuVisibilityRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        MenuResponse response =
                menuService.updateVisibility(
                        menuId, request.hidden(), principal.getId(), hasElevatedRole(principal));
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "메뉴 숨김 상태가 변경되었습니다.", response));
    }

    // 메뉴 삭제 (Soft Delete)
    @Operation(summary = "메뉴 삭제", description = "메뉴를 소프트 삭제 처리합니다.")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId, @AuthenticationPrincipal CustomUserDetails principal) {
        // CustomAuditorAware/AddressService와 동일한 "{id}_{username}" 포맷으로 통일
        menuService.deleteMenu(
                menuId,
                principal.getId() + "_" + principal.getUsername(),
                principal.getId(),
                hasElevatedRole(principal));
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "메뉴가 삭제되었습니다.", null));
    }

    // MANAGER/MASTER는 플랫폼 관리자로서 가게 소유권 검증을 우회함 - OWNER만 자기 가게로 제한
    private boolean hasElevatedRole(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(
                        authority ->
                                authority.equals(Role.Authority.MANAGER)
                                        || authority.equals(Role.Authority.MASTER));
    }
}
