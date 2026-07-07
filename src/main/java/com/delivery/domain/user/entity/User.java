package com.delivery.domain.user.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_user")
public class User extends BaseEntity {
    // TODO : BaseEntity 세팅해야함
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false)
    private String tel;

    @Column(nullable = false)
    private UserStatus userStatus;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "p_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
