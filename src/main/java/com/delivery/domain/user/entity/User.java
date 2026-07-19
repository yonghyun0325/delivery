package com.delivery.domain.user.entity;

import static com.delivery.domain.user.entity.Role.*;

import com.delivery.common.base.BaseEntity;
import com.delivery.common.util.CryptoConverter;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.Set;
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
@Table(name = "p_user")
public class User extends BaseEntity {
    // CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 50;
    // TODO : 추후 User 테이블 명세서 Erd 수정해야함
    @Id
    @Column(name = "user_id")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 60)
    private String nickName;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, length = 255)
    private String phoneNumber;

    @Column(nullable = false)
    private UserStatus userStatus;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "p_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    // TODO : API 문서 및 데이터 정의서, ERD에 UUID 컬럼 추가
    @Column(nullable = false, unique = true, name = "user_uuid")
    UUID userUuid;

    public static User create(
            String username,
            String password,
            String nickName,
            String phoneNumber,
            Set<Role> roles) {
        return User.builder()
                .username(username)
                .password(password)
                .nickName(nickName)
                .phoneNumber(phoneNumber)
                .roles(roles)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    public void updatedPassword(String password) {
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
    }

    public void updateNickName(String nickName) {
        if (nickName != null && !nickName.isBlank()) {
            this.nickName = nickName;
        }
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateRoles(Role role) {
        this.roles =
                switch (role) {
                    case CUSTOMER -> Set.of(CUSTOMER);
                    case OWNER -> Set.of(CUSTOMER, OWNER);
                    case MANAGER -> Set.of(CUSTOMER, OWNER, MANAGER);
                    case MASTER -> Set.of(CUSTOMER, OWNER, MANAGER, MASTER);
                };
    }

    public void delete(String deletedBy) {
        this.userStatus = UserStatus.DELETED;
        this.username = this.username + "_" + UUID.randomUUID().toString().substring(0, 8);
        this.nickName = "탈퇴회원" + "_" + UUID.randomUUID().toString().substring(0, 8);
        super.delete(this.getId() + "_" + deletedBy);
    }

    public static String maskingPhoneNumber(String phoneNumber) {
        return phoneNumber.substring(0, phoneNumber.length() - 4) + "****";
    }

    @PrePersist
    protected void onCreate() {
        if (this.userUuid == null) {
            this.userUuid = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
