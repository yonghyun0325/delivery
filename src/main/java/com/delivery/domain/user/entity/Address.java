package com.delivery.domain.user.entity;

import com.delivery.common.base.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String addressDetail;

    @Column(nullable = false)
    private boolean isDefault;

    public static Address create(
            User user, String address, String addressDetail, boolean isDefault) {
        return Address.builder()
                .user(user)
                .address(address)
                .addressDetail(addressDetail)
                .isDefault(isDefault)
                .createdBy(user.getId() + "_" + user.getUsername())
                .build();
    }

    public void update(String address, String AddressDetail, boolean isDefault) {
        this.address = address;
        this.addressDetail = AddressDetail;
        this.isDefault = isDefault;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
