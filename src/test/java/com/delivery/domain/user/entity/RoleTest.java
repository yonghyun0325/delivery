package com.delivery.domain.user.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.delivery.domain.user.exception.AuthException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {
    private Role role;

    @Test
    @DisplayName("MANAGER나 권한이면 INVALID_ROLE 예외를 발생시킨다.")
    void validateSignupRole() {
        // given
        Role role = Role.MANAGER;

        // when & then
        assertThatThrownBy(() -> Role.validateSignupRole(role)).isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("매개변수가 Owner 일 떄 CUSTOMER, OWNER 권한을 둘 다 넣는다.")
    void getDefaultRoles() {
        // when
        Set<Role> roles = Role.getDefaultRoles(Role.OWNER);

        // then
        assertThat(roles).contains(Role.CUSTOMER);
        assertThat(roles).contains(Role.OWNER);
    }
}
