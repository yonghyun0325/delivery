package com.delivery.domain.user.service;

import com.delivery.common.util.SsnEncryptor;
import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.entity.Address;
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
    private final SsnEncryptor ssnEncryptor;

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

    //    private AddressResponse toDto(Address address) {
    //        String decryptedAddress = ssnEncryptor.decrypt(address.getAddress());
    //        String decryptedAddressDetail = ssnEncryptor.decrypt(address.getAddressDetail());
    //        return UserDtoMapper.toDto(address,  decryptedAddress, decryptedAddressDetail);
    //    }
}
