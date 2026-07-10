package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.AddressResponseDto;
import com.delivery.domain.user.dto.CreateAddressRequest;
import com.delivery.domain.user.dto.UpdateAddressRequestDto;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.mapper.UserDtoMapper;
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
    private final UserService userService;

    // TODO : 테스트 시 삭제 배송지 확인해봐야함, 동시성 문제 생길 확률 높음
    public AddressResponseDto createAddress(Long userId, CreateAddressRequest request) {
        if (addressRepository.countByUserIdAndDeletedAtIsNull(userId) >= 10) {
            throw new UserException(UserErrorCode.EXCEED_MAX_ADDRESS);
        }

        if (request.getIsDefault()) {
            resetDefault(userId);
        }

        User user = userService.findActiveUser(userId);

        Address address =
                Address.create(
                        user,
                        request.getAddress(),
                        request.getAddressDetail(),
                        request.getIsDefault());
        return UserDtoMapper.toDto(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDto> findAddresses(Long userId) {
        return addressRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(UserDtoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponseDto findAddress(Long userId, UUID addressId) {
        return UserDtoMapper.toDto(
                addressRepository
                        .findByIdAndUserIdAndDeletedAtIsNull(addressId, userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_ADDRESS)));
    }

    @Transactional
    public AddressResponseDto updateAddress(
            Long userId, UUID addressId, UpdateAddressRequestDto request) {
        if (request.getIsDefault()) {
            resetDefault(userId);
        }

        Address address =
                addressRepository
                        .findByIdAndUserIdAndDeletedAtIsNull(addressId, userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_ADDRESS));
        address.update(request.getAddress(), request.getAddressDetail(), request.getIsDefault());

        return UserDtoMapper.toDto(address);
    }

    @Transactional
    public void deleteAddress(Long userId, String username, UUID addressId) {
        Address address =
                addressRepository
                        .findByIdAndUserIdAndDeletedAtIsNull(addressId, userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_ADDRESS));
        address.delete(userId + "_" + username);
    }

    private void resetDefault(Long userId) {
        addressRepository
                .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                .ifPresent(address -> address.updateDefault(false));
    }
}
