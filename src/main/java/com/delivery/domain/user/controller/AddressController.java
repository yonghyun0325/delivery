package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.controller.swagger.AddressApi;
import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.service.AddressService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 배송지 컨트롤러 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/users/me/addresses")
public class AddressController implements AddressApi {
    private final AddressService addressService;

    @Override
    @PostMapping
    public ResponseEntity<RestApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED,
                                "배송지 추가 성공",
                                addressService.createAddress(customUserDetails.getId(), request)));
    }

    @Override
    @GetMapping
    public ResponseEntity<RestApiResponse<List<AddressResponse>>> getAddressList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 전체 조회 성공",
                        addressService.findAddresses(customUserDetails.getId())));
    }

    @Override
    @GetMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<AddressResponse>> getAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 조회 성공",
                        addressService.findAddress(customUserDetails.getId(), addressId)));
    }

    // TODO : 배송지 수정 시 Body를 반환하도록 변경 API 문서 수정해야함.
    @Override
    @PatchMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 수정 성공",
                        addressService.updateAddress(
                                customUserDetails.getId(), addressId, request)));
    }

    @Override
    @DeleteMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId) {
        addressService.deleteAddress(
                customUserDetails.getId(), customUserDetails.getUsername(), addressId);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "배송지 삭제 성공", null));
    }
}
