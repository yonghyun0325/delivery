package com.delivery.domain.user.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.common.util.CryptoConverter;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_address")
public class Address extends BaseEntity {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, length = 255)
    private String address;

    // TODO : 255로 명세서랑 ERD 수정
    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, length = 255)
    private String addressDetail;

    @Column(nullable = false)
    private boolean isDefault;

    public static Address create(
            long userId, String address, String addressDetail, boolean isDefault) {
        return Address.builder()
                .userId(userId)
                .address(address)
                .addressDetail(addressDetail)
                .isDefault(isDefault)
                .build();
    }

    public void update(String address, String addressDetail, boolean isDefault) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
