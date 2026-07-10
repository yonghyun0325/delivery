package com.delivery.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.AddressRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceUnitTest {
    @Mock private AddressRepository addressRepository;
    @Mock private UserService userService;
    @InjectMocks private AddressService addressService;

    @Test
    @DisplayName("이미 배송지가 10개 이상 등록되어 있을 때 배송지 등록에 실패한다.")
    void createAddress_fail_when_exceed_limit() {
        // given
        long userId = 1L;
        CreateAddressRequest request = new CreateAddressRequest("주소", "상세주소", false);
        when(addressRepository.countByUserIdAndDeletedAtIsNull(eq(userId))).thenReturn(10L);

        // when & then
        assertThatThrownBy(() -> addressService.createAddress(userId, request))
                .isInstanceOf(UserException.class)
                .hasMessage("배송지는 최대 10개까지 등록할 수 있습니다.");

        verify(addressRepository).countByUserIdAndDeletedAtIsNull(userId);
        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 배송지를 조회할 시 UserException 예외가 발생한다.")
    void findAddress_fail_when_not_exist() {
        // given
        long userId = 1L;
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findByIdAndUserIdAndDeletedAtIsNull(any(), eq(userId)))
                .thenThrow(new UserException(UserErrorCode.NOT_EXIST_ADDRESS));

        // when & then
        assertThatThrownBy(() -> addressService.findAddress(userId, addressId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 배송지입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 배송지를 수정하려할 시 UserException 예외가 발생한다.")
    void updateAddress_fail_when_not_exist() {
        // given
        long userId = 1L;
        UUID addressId = UUID.randomUUID();
        UpdateAddressRequest request = new UpdateAddressRequest("주소", "상세주소", false);
        when(addressRepository.findByIdAndUserIdAndDeletedAtIsNull(any(), eq(userId)))
                .thenThrow(new UserException(UserErrorCode.NOT_EXIST_ADDRESS));

        // when & then
        assertThatThrownBy(() -> addressService.updateAddress(userId, addressId, request))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 배송지입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 배송지를 삭제하려할 시 UserException 예외가 발생한다..")
    void deleteAddress_fail_when_not_exist() {
        // given
        long userId = 1L;
        String username = "테스트";
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findByIdAndUserIdAndDeletedAtIsNull(any(), eq(userId)))
                .thenThrow(new UserException(UserErrorCode.NOT_EXIST_ADDRESS));

        // when & then
        assertThatThrownBy(() -> addressService.deleteAddress(userId, username, addressId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 배송지입니다.");
    }
}
