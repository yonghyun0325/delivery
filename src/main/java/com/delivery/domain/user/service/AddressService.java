package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
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
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        if (addressRepository.countByUserIdAndDeletedAtIsNull(userId) >= 10) {
            throw new UserException(UserErrorCode.EXCEED_MAX_ADDRESS);
        }

        User user = userService.findActiveUser(userId);

        if (request.isDefault()) {
            resetDefault(userId);
        }

        Address address =
                Address.create(
                        user, request.address(), request.addressDetail(), request.isDefault());
        return UserDtoMapper.toDto(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAddresses(Long userId) {
        return addressRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(userId).stream()
                .map(UserDtoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse findAddress(Long userId, UUID addressId) {
        return UserDtoMapper.toDto(findAddressOrThrow(addressId, userId));
    }

    public AddressResponse updateAddress(
            Long userId, UUID addressId, UpdateAddressRequest request) {
        Address address = findAddressOrThrow(addressId, userId);

        if (request.isDefault()) {
            resetDefault(userId);
        }

        address.update(request.address(), request.addressDetail(), request.isDefault());

        return UserDtoMapper.toDto(address);
    }

    public void deleteAddress(Long userId, String username, UUID addressId) {
        Address address = findAddressOrThrow(addressId, userId);
        address.delete(userId + "_" + username);
    }

    private Address findAddressOrThrow(UUID addressId, Long userId) {
        return addressRepository
                .findByIdAndUserIdAndDeletedAtIsNull(addressId, userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_ADDRESS));
    }

    private void resetDefault(Long userId) {
        addressRepository
                .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                .ifPresent(address -> address.updateDefault(false));
    }
}
