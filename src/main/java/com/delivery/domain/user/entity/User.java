package com.delivery.domain.user.entity;

import com.delivery.common.base.BaseEntity;
import com.delivery.common.util.CryptoConverter;
import com.delivery.domain.user.enums.Role;
import com.delivery.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_user")
public class User extends BaseEntity {
    // CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 50;
    @Id
    @Column(name = "user_id")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
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
}
