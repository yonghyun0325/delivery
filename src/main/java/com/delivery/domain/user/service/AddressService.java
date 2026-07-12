package com.delivery.domain.user.service;

import com.delivery.common.util.SsnEncryptor;
import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.AddressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final SsnEncryptor ssnEncryptor;

    /**
     * 배송지 생성 배송지가 10개 이상인지 검증 배송지 수정 요청 DTO에서 기본 배송지인지 체크 기본 배송지일시 기존의 기본 배송지 Fasle로 상태 변경
     *
     * @param userId 회원 PK키
     * @param request 배송지 생성 요청 DTO
     * @return 생성한 배송지 응답 DTO
     */
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        if (addressRepository.countByUserIdAndDeletedAtIsNull(userId) >= 10) {
            throw new UserException(UserErrorCode.EXCEED_MAX_ADDRESS);
        }

        if (request.isDefault()) {
            resetDefault(userId);
        }

        Address address =
                Address.create(
                        userId, request.address(), request.addressDetail(), request.isDefault());
        return UserDtoMapper.toAddressResponse(addressRepository.save(address));
    }

    /**
     * 배송지 목록 조회
     *
     * @param userId 회원 PK키
     * @return 배송지 목록 조회 응답 DTO
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> findAddresses(Long userId) {
        return addressRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(userId).stream()
                .map(UserDtoMapper::toAddressResponse)
                .toList();
    }

    /**
     * 특정 배송지 조회
     *
     * @param userId 회원 PK키
     * @param addressId 조회할 배송지 PK키
     * @return 배송지 조회 응답 DTO
     */
    @Transactional(readOnly = true)
    public AddressResponse findAddress(Long userId, UUID addressId) {
        return UserDtoMapper.toAddressResponse(findAddressOrThrow(addressId, userId));
    }

    /**
     * 배송지 수정 배송지 수정 요청 DTO에서 기본 배송지 설정 유무 체크 True일 시 기존 기본 배송지 False로 상태 변경
     *
     * @param userId 회원 PK키
     * @param addressId 수정할 배송지 PK키
     * @param request 배송지 수정 요청 DTO
     * @return 배송지 수정 응답 DTo
     */
    public AddressResponse updateAddress(
            Long userId, UUID addressId, UpdateAddressRequest request) {
        Address address = findAddressOrThrow(addressId, userId);

        if (request.isDefault()) {
            resetDefault(userId);
        }

        address.update(request.address(), request.addressDetail(), request.isDefault());

        return UserDtoMapper.toAddressResponse(address);
    }

    /**
     * 배송지 삭제
     *
     * @param userId
     * @param username
     * @param addressId
     */
    public void deleteAddress(Long userId, String username, UUID addressId) {
        Address address = findAddressOrThrow(addressId, userId);
        address.delete(userId + "_" + username);
    }

    /**
     * 배송지 조회 및 예외처리
     *
     * @param addressId 조회할 배송지 PK키
     * @param userId 회원 PK키
     * @return 조회한 Address 엔티티 객체
     */
    @Transactional(readOnly = true)
    private Address findAddressOrThrow(UUID addressId, Long userId) {
        return addressRepository
                .findByIdAndUserIdAndDeletedAtIsNull(addressId, userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_ADDRESS));
    }

    /**
     * 기존 기본 배송지 비활성화
     *
     * @param userId 회원 PK키
     */
    private void resetDefault(Long userId) {
        addressRepository
                .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                .ifPresent(address -> address.updateDefault(false));
    }
}
