package com.delivery.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.user.dto.request.CreateAddressRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.request.UpdateAddressRequest;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.fixture.AddressFixture;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.AddressRepository;
import com.delivery.domain.user.repository.UserRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class AddressServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private AddressService addressService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;

    private User savedUser;
    // 0번 기본 true, 1번 False
    private List<AddressResponse> setUpAddresses = new ArrayList<>();
    private long userId;
    private String username;

    @BeforeEach
    void setUp() {
        SignUpRequest request = UserFixture.ROLE_CUSTOMER.createRequestDto();

        String encodedPassword = passwordEncoder.encode(request.password());
        Set<Role> roles = Role.getDefaultRoles(request.role());

        savedUser =
                userRepository.save(
                        User.create(
                                request.username(),
                                encodedPassword,
                                request.nickName(),
                                request.phoneNumber(),
                                roles));
        List<CreateAddressRequest> addresses = new ArrayList<>();
        userId = savedUser.getId();
        username = savedUser.getUsername();

        addresses.add(AddressFixture.DEFAULT_TRUE.createRequestDto());
        addresses.add(AddressFixture.DEFAULT_FALSE.createRequestDto());

        for (CreateAddressRequest address : addresses) {
            setUpAddresses.add(addressService.createAddress(userId, address));
        }
    }

    @Nested
    @DisplayName("배송지 등록 테스트")
    class CreateAddress {

        /** TODO : 멱등성 테스트는 추후 진행 예정 */
        @Test
        @Transactional
        @DisplayName("배송지 등록 성공 - 정상 저장 확인")
        void createAddress_success() {
            // given
            CreateAddressRequest createAddressRequest =
                    AddressFixture.DEFAULT_FALSE.createRequestDto();

            // when
            AddressResponse savedAddress =
                    addressService.createAddress(userId, createAddressRequest);

            // then
            assertThat(savedAddress.address()).isEqualTo(createAddressRequest.address());
            assertThat(savedAddress.addressDetail())
                    .isEqualTo(createAddressRequest.addressDetail());
            assertThat(savedAddress.isDefault()).isEqualTo(createAddressRequest.isDefault());
        }

        @Test
        @Transactional
        @DisplayName("기본 배송지 체크 후 배송지 등록 시 기존 기본 배송지 설정 해제")
        void createAddress_success_with_default() {
            // given
            CreateAddressRequest createAddressRequest =
                    AddressFixture.DEFAULT_TRUE.createRequestDto();

            // when
            AddressResponse savedAddress =
                    addressService.createAddress(userId, createAddressRequest);

            Address defaultAddress =
                    addressRepository
                            .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                            .orElseThrow();

            // then
            assertThat(setUpAddresses.get(0).isDefault()).isTrue();
            assertThat(savedAddress.addressId()).isEqualTo(defaultAddress.getId());
        }
    }

    @Nested
    @Transactional
    @DisplayName("배송지 수정 테스트")
    class UpdateAddress {
        @Test
        @DisplayName("배송지 수정 성공")
        void updateAddress_success() {
            // given
            UpdateAddressRequest updateAddressRequest =
                    new UpdateAddressRequest("수정주소", "수정상세주소", false);
            UUID tagetAddressId = setUpAddresses.get(0).addressId();

            // when
            var updatedAddress =
                    addressService.updateAddress(userId, tagetAddressId, updateAddressRequest);

            // then
            assertThat(updatedAddress.addressId()).isEqualTo(tagetAddressId);
            assertThat(updatedAddress.address()).isEqualTo(updateAddressRequest.address());
            assertThat(updatedAddress.addressDetail())
                    .isEqualTo(updateAddressRequest.addressDetail());
            assertThat(updatedAddress.isDefault()).isEqualTo(updateAddressRequest.isDefault());
        }

        @Test
        @DisplayName("다른 사람의 배송지 수정 시 예외 발생")
        void updateAddress_fail_when_UserDoesNotOwnAddress() {
            // given
            UpdateAddressRequest updateAddressRequest =
                    new UpdateAddressRequest("수정주소", "수정상세주소", false);

            // when
            Address address =
                    addressRepository
                            .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                            .orElseThrow();

            // then
            assertThatThrownBy(
                            () ->
                                    addressService.updateAddress(
                                            10L, address.getId(), updateAddressRequest))
                    .isInstanceOf(UserException.class)
                    .hasMessage("존재하지 않는 배송지입니다.");
        }

        @Test
        @DisplayName("특정 배송지를 기본 배송지로 업데이트 시 기존 기본값 해제")
        void update_To_Default_Address_updates_Previous_Default_To_False() {
            // given
            UpdateAddressRequest updateAddressRequest =
                    new UpdateAddressRequest("수정주소", "수정상세주소", true);

            // when
            AddressResponse updatedAddress =
                    addressService.updateAddress(
                            userId, setUpAddresses.get(1).addressId(), updateAddressRequest);

            Address defaultAddress =
                    addressRepository
                            .findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(userId)
                            .orElseThrow();

            // then
            assertThat(setUpAddresses.get(0).isDefault()).isTrue();
            assertThat(updatedAddress.addressId()).isEqualTo(defaultAddress.getId());
        }
    }

    @Test
    @Transactional
    @DisplayName("배송지 삭제 시 Soft Delete 적용 및 조회 불가 확인")
    void deleteAddress_success() {
        // given
        UUID addressId = setUpAddresses.get(0).addressId();

        // when
        addressService.deleteAddress(userId, username, addressId);
        Address address = addressRepository.findById(addressId).orElseThrow();

        // then
        assertThat(address.getDeletedAt()).isNotNull();
        assertThatThrownBy(() -> addressService.findAddress(userId, addressId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 배송지입니다.");
    }

    @Test
    @Transactional
    @DisplayName("주소 암호화 여부 테스트")
    void encode_address_success() {
        // given
        String address = "서울 강남구 테헤란로 311";
        String addressDetail = "(역삼동, 아남타워빌딩) 3층";
        String sql = "select * from p_address where address_id = ?";

        // when
        Map<String, Object> setUpAddress =
                jdbcTemplate.queryForMap(sql, setUpAddresses.get(0).addressId());

        // then
        assertThat(setUpAddress.get("address")).isNotEqualTo(address);
        assertThat(setUpAddress.get("address_detail")).isNotEqualTo(addressDetail);
    }

    @Test
    @Transactional
    @DisplayName("주소 복호화 여부 테스트")
    void decode_address_success() {
        // given
        String address = "서울 강남구 테헤란로 311";
        String addressDetail = "(역삼동, 아남타워빌딩) 3층";

        // when
        var setUpAddress = addressService.findAddress(userId, setUpAddresses.get(0).addressId());

        // then
        assertThat(setUpAddress.address()).isEqualTo(address);
        assertThat(setUpAddress.addressDetail()).isEqualTo(addressDetail);
    }
}
