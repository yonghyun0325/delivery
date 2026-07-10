package com.delivery.domain.menu.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.menu.dto.request.CreateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuRequest;
import com.delivery.domain.menu.dto.request.UpdateMenuVisibilityRequest;
import com.delivery.domain.menu.dto.response.MenuResponse;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메뉴", description = "메뉴 CRUD 및 AI 설명 생성 API")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private static final String STORE_NOT_LINKED_NOTICE =
            " (Store 도메인 연동 전이라 현재는 Role 기반 권한만 검증하며, 실제 가게 소유권 검증은 하지 않습니다.)";

    private final MenuService menuService;

    // 메뉴 등록
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @Operation(
            summary = "메뉴 등록",
            description = "가게에 메뉴를 등록합니다. AI 설명 생성 옵션을 지원합니다." + STORE_NOT_LINKED_NOTICE)
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<MenuResponse>> createMenu(
            @PathVariable UUID storeId, @Valid @RequestBody CreateMenuRequest request) {
        MenuResponse response =
                menuService.createMenu(
                        storeId,
                        request.name(),
                        request.description(),
                        request.price(),
                        request.aiGeneration(),
                        request.aiPrompt());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "메뉴가 등록되었습니다.", response));
    }

    // 메뉴 목록 조회
    @Operation(summary = "가게별 메뉴 목록 조회", description = "가게에 등록된 삭제되지 않은 메뉴 목록을 조회합니다.")
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<List<MenuResponse>>> getStoreMenus(
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK, "메뉴 목록 조회에 성공했습니다.", menuService.getStoreMenus(storeId)));
    }

    // 메뉴 단건 조회
    @Operation(summary = "메뉴 단건 조회", description = "메뉴 ID로 메뉴 한 건을 조회합니다.")
    @GetMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<MenuResponse>> getMenu(@PathVariable UUID menuId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK, "메뉴 조회에 성공했습니다.", menuService.getMenu(menuId)));
    }

    // 메뉴 수정
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @Operation(summary = "메뉴 수정", description = "메뉴의 이름/설명/가격을 수정합니다." + STORE_NOT_LINKED_NOTICE)
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<MenuResponse>> updateMenu(
            @PathVariable UUID menuId, @Valid @RequestBody UpdateMenuRequest request) {
        MenuResponse response =
                menuService.updateMenu(
                        menuId, request.name(), request.description(), request.price());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "메뉴가 수정되었습니다.", response));
    }

    // 숨김 상태 업데이트
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @Operation(summary = "메뉴 숨김 상태 변경", description = "메뉴를 숨김/노출 처리합니다." + STORE_NOT_LINKED_NOTICE)
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}/visibility")
    public ResponseEntity<RestApiResponse<MenuResponse>> updateMenuVisibility(
            @PathVariable UUID menuId, @RequestBody UpdateMenuVisibilityRequest request) {
        MenuResponse response = menuService.updateVisibility(menuId, request.hidden());
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "메뉴 숨김 상태가 변경되었습니다.", response));
    }

    // 메뉴 삭제 (Soft Delete)
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @Operation(summary = "메뉴 삭제", description = "메뉴를 소프트 삭제 처리합니다." + STORE_NOT_LINKED_NOTICE)
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId, @AuthenticationPrincipal CustomUserDetails principal) {
        // CustomAuditorAware/AddressService와 동일한 "{id}_{username}" 포맷으로 통일
        menuService.deleteMenu(menuId, principal.getId() + "_" + principal.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "메뉴가 삭제되었습니다.", null));
    }
}
