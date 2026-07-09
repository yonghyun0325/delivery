package com.delivery.domain.menu.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.menu.dto.request.ReqCreateMenuDtoV1;
import com.delivery.domain.menu.dto.request.ReqUpdateMenuDtoV1;
import com.delivery.domain.menu.dto.request.ReqUpdateMenuVisibilityDtoV1;
import com.delivery.domain.menu.dto.response.ResMenuDtoV1;
import com.delivery.domain.menu.entity.MenuEntity;
import com.delivery.domain.menu.service.MenuServiceV1;
import com.delivery.global.security.config.CustomUserDetails;
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

@RestController
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuService;

    // 메뉴 등록
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<ResMenuDtoV1>> createMenu(
            @PathVariable UUID storeId, @Valid @RequestBody ReqCreateMenuDtoV1 request) {
        MenuEntity menu =
                menuService.createMenu(
                        storeId,
                        request.name(),
                        request.description(),
                        request.price(),
                        request.aiGeneration(),
                        request.aiPrompt());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED, "메뉴가 등록되었습니다.", ResMenuDtoV1.from(menu)));
    }

    // 메뉴 목록 조회
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<RestApiResponse<List<ResMenuDtoV1>>> getStoreMenus(
            @PathVariable UUID storeId) {
        List<ResMenuDtoV1> menus =
                menuService.getStoreMenus(storeId).stream().map(ResMenuDtoV1::from).toList();
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "메뉴 목록 조회에 성공했습니다.", menus));
    }

    // 메뉴 단건 조회
    @GetMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<ResMenuDtoV1>> getMenu(@PathVariable UUID menuId) {
        MenuEntity menu = menuService.getMenu(menuId);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "메뉴 조회에 성공했습니다.", ResMenuDtoV1.from(menu)));
    }

    // 메뉴 수정
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<ResMenuDtoV1>> updateMenu(
            @PathVariable UUID menuId, @Valid @RequestBody ReqUpdateMenuDtoV1 request) {
        MenuEntity menu =
                menuService.updateMenu(
                        menuId, request.name(), request.description(), request.price());
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "메뉴가 수정되었습니다.", ResMenuDtoV1.from(menu)));
    }

    // 숨김 상태 업데이트
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}/visibility")
    public ResponseEntity<RestApiResponse<ResMenuDtoV1>> updateMenuVisibility(
            @PathVariable UUID menuId, @RequestBody ReqUpdateMenuVisibilityDtoV1 request) {
        MenuEntity menu = menuService.updateVisibility(menuId, request.hidden());
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK, "메뉴 숨김 상태가 변경되었습니다.", ResMenuDtoV1.from(menu)));
    }

    // 메뉴 삭제 (Soft Delete)
    // TODO: Store 연동 후 실제 소유권(owner) 검증 추가 필요 — 지금은 역할(Role)만 체크
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<RestApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId, @AuthenticationPrincipal CustomUserDetails principal) {
        menuService.deleteMenu(menuId, principal.getUsername());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "메뉴가 삭제되었습니다.", null));
    }
}
