package com.delivery.domain.user.fixture;

import com.delivery.domain.user.dto.request.CreateAddressRequest;
import lombok.AllArgsConstructor;

/** 배송지 생성용 */
@AllArgsConstructor
public enum AddressFixture {
    DEFAULT_TRUE("서울 강남구 테헤란로 311", "(역삼동, 아남타워빌딩) 3층", true),
    DEFAULT_FALSE("서울 강남구 테헤란로 311", "(역삼동, 아남타워빌딩) 3층", false);

    private final String address;
    private final String addressDetail;
    private final boolean isDefault;

    // Create DTO
    public CreateAddressRequest createRequestDto() {
        return CreateAddressRequest.builder()
                .address(address)
                .addressDetail(addressDetail)
                .isDefault(isDefault)
                .build();
    }
}
