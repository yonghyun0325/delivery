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

    public AddressResponseDto createAddress(Long userId, CreateAddressRequest request) {
        if (addressRepository.countByUserId(userId) == 10) {
            throw new UserException(UserErrorCode.EXCEED_MAX_ADDRESS);
        }
        if (request.getIsDefault() == true
                && addressRepository.existsByUserIdAndIsDefault(userId, true)) {
            throw new UserException(UserErrorCode.ALREADY_EXISTS_DEFAULT_ADDRESS);
        }

        User user = userService.findUser(userId);

        Address address =
                Address.create(
                        user,
                        request.getAddress(),
                        request.getAddressDetail(),
                        request.getIsDefault());
        return UserDtoMapper.toDto(addressRepository.save(address));
    }

    public List<AddressResponseDto> findAddresses(Long userId) {
        return addressRepository.findAllByUserId(userId).stream()
                .map(UserDtoMapper::toDto)
                .toList();
    }

    public AddressResponseDto findAddress(Long userId, UUID addressId) {
        return UserDtoMapper.toDto(addressRepository.findByIdAndUserId(addressId, userId));
    }

    public AddressResponseDto updateAddress(
            Long userId, UUID addressId, UpdateAddressRequestDto request) {
        throw new UnsupportedOperationException("개발 중");
    }

    public void deleteAddress(Long userId, UUID addressId) {
        throw new UnsupportedOperationException("개발 중");
    }
}
