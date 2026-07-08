package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.AddressResponseDto;
import com.delivery.domain.user.dto.CreateAddressRequest;
import com.delivery.domain.user.dto.UpdateAddressRequestDto;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.entity.User;
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
        throw new UnsupportedOperationException("개발 중");
    }

    public AddressResponseDto findAddress(Long userId, UUID addressId) {
        throw new UnsupportedOperationException("개발 중");
    }

    public AddressResponseDto updateAddress(
            Long userId, UUID addressId, UpdateAddressRequestDto request) {
        throw new UnsupportedOperationException("개발 중");
    }

    public void deleteAddress(Long userId, UUID addressId) {
        throw new UnsupportedOperationException("개발 중");
    }
}
