package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.AddressResponseDto;
import com.delivery.domain.user.dto.CreateAddressRequest;
import com.delivery.domain.user.dto.UpdateAddressRequestDto;
import com.delivery.domain.user.service.AddressService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/addresses")
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<RestApiResponse<AddressResponseDto>> createAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED,
                                "배송지 추가 성공",
                                addressService.createAddress(customUserDetails.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<List<AddressResponseDto>>> getAddressList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 전체 조회 성공",
                        addressService.findAddresses(customUserDetails.getId())));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<AddressResponseDto>> getAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 조회 성공",
                        addressService.findAddress(customUserDetails.getId(), addressId)));
    }

    // TODO : 배송지 수정 시 Body를 반환하도록 변경 API 문서 수정해야함.
    @PatchMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<AddressResponseDto>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequestDto request) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "배송지 수정 성공",
                        addressService.updateAddress(
                                customUserDetails.getId(), addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<RestApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable UUID addressId) {
        addressService.deleteAddress(
                customUserDetails.getId(), customUserDetails.getUsername(), addressId);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "배송지 삭제 성공", null));
    }
}
