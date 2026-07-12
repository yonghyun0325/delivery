package com.delivery.domain.user.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.service.AddressService;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.testconfig.WithMockCustomUser;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerUnitTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AddressService addressService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtRequestFilter jwtRequestFilter;
    @MockitoBean ErrorCodeRegistry errorCodeRegistry;

    private final UUID addressId = UUID.randomUUID();
    private final CreateAddressRequest request = new CreateAddressRequest("주소1", "상세주소1", true);

    @Nested
    @DisplayName("배송지 등록")
    class CreateAddress {
        @Test
        @WithMockCustomUser(id = 1L, role = "CUSTOMER")
        @DisplayName("로그인 유저가 배송지 생성에 성공한다.")
        void createAddress_success() throws Exception {
            // given
            AddressResponse response =
                    new AddressResponse(
                            addressId,
                            request.address(),
                            request.addressDetail(),
                            request.isDefault());

            given(addressService.createAddress(eq(1L), eq(request))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/users/me/addresses")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("배송지 추가 성공"))
                    .andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
                    .andExpect(jsonPath("$.data.address").value(request.address()))
                    .andExpect(jsonPath("$.data.addressDetail").value(request.addressDetail()))
                    .andExpect(jsonPath("$.data.isDefault").value(request.isDefault()));
        }

        @Test
        @WithMockCustomUser(id = 1L, role = "CUSTOMER")
        @DisplayName("배송지 입력 값이 비었을 때 INVALID_PARAMETER_TYPE 예외를 반환한다.")
        void createAddress_fail() throws Exception {
            // given
            CreateAddressRequest request = new CreateAddressRequest("", "상세주소1", true);

            // when & then
            mockMvc.perform(
                            post("/api/v1/users/me/addresses")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("INVALID_PARAMETER_TYPE"));
        }
    }

    @Nested
    @DisplayName("배송지 조회 테스트")
    @WithMockCustomUser(id = 1L, role = "CUSTOMER")
    class GetAddress {
        @Test
        @DisplayName("배송지 조회 성공 시 200과 배송지 정보를 반환한다..")
        void getAddress_success() throws Exception {
            // given
            AddressResponse response = new AddressResponse(addressId, "주소1", "상세주소1", true);
            given(addressService.findAddress(eq(1L), eq(addressId))).willReturn(response);

            // when & then
            mockMvc.perform(
                            get("/api/v1/users/me/addresses/{addressId}", addressId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
                    .andExpect(jsonPath("$.data.address").value("주소1"))
                    .andExpect(jsonPath("$.data.addressDetail").value("상세주소1"));
        }

        @Test
        @DisplayName("배송지 조회 시 배송지가 없으면 NOT_EXIST_ADDRESS(404) 에러를 반환한다.")
        void getAddress_fail_whenNotExistAddress() throws Exception {
            // given
            given(addressService.findAddress(eq(1L), eq(addressId)))
                    .willThrow(new UserException(UserErrorCode.NOT_EXIST_ADDRESS));

            // when & then
            mockMvc.perform(
                            get("/api/v1/users/me/addresses/{addressId}", addressId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("NOT_EXIST_ADDRESS"));
        }

        @Test
        @DisplayName("배송지 목록 조회 시 200과 배송지 LIST를 반환한다.")
        void getAddressList_success() throws Exception {
            // given
            List<AddressResponse> responsesList =
                    List.of(
                            new AddressResponse(UUID.randomUUID(), "주소1", "상세주소1", false),
                            new AddressResponse(UUID.randomUUID(), "주소2", "상세주소2", false));

            given(addressService.findAddresses(eq(1L))).willReturn(responsesList);

            // when & then
            mockMvc.perform(
                            get("/api/v1/users/me/addresses")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].address").value("주소1"))
                    .andExpect(jsonPath("$.data[1].address").value("주소2"));
        }
    }

    @Nested
    @DisplayName("배송지 수정 테스트")
    @WithMockCustomUser(id = 1L, role = "CUSTOMER")
    class UpdateAddress {
        @Test
        @DisplayName("등록한 배송지를 수정 한다.")
        void updateAddress_success() throws Exception {
            // given
            UpdateAddressRequest updateRequest =
                    new UpdateAddressRequest("업데이트 주소", "업데이트 상세주소", true);

            AddressResponse response =
                    new AddressResponse(
                            addressId,
                            updateRequest.address(),
                            updateRequest.addressDetail(),
                            updateRequest.isDefault());

            given(addressService.updateAddress(eq(1L), eq(addressId), eq(updateRequest)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(
                            patch("/api/v1/users/me/addresses/{addressId}", addressId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
                    .andExpect(jsonPath("$.data.address").value(updateRequest.address()))
                    .andExpect(
                            jsonPath("$.data.addressDetail").value(updateRequest.addressDetail()))
                    .andExpect(jsonPath("$.data.isDefault").value(updateRequest.isDefault()));
        }
    }

    @Test
    @WithMockCustomUser(id = 1L, role = "CUSTOMER")
    @DisplayName("자신이 등록한 배송지 삭제에 성공한다.")
    void deleteAddress_success() throws Exception {
        // given
        willDoNothing().given(addressService).deleteAddress(eq(1L), anyString(), eq(addressId));

        // when & then
        mockMvc.perform(
                        delete("/api/v1/users/me/addresses/{addressId}", addressId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("배송지 삭제 성공"));
    }
}
