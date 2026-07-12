package com.delivery.domain.user.entity;

import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    MASTER(Authority.MASTER),
    MANAGER(Authority.MANAGER),
    OWNER(Authority.OWNER),
    CUSTOMER(Authority.CUSTOMER);

    private final String authority;

    public static void validateSignupRole(Role role) {
        if (role.equals(Role.MANAGER) || role.equals(Role.MASTER)) {
            throw new AuthException(AuthErrorCode.INVALID_ROLE);
        }
    }

    public static Set<Role> getDefaultRoles(Role role) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CUSTOMER);

        if (Role.OWNER.equals(role)) {
            roles.add(Role.OWNER);
        }

        return roles;
    }

    public static class Authority {
        public static final String MASTER = "ROLE_MASTER";
        public static final String MANAGER = "ROLE_MANAGER";
        public static final String OWNER = "ROLE_OWNER";
        public static final String CUSTOMER = "ROLE_CUSTOMER";
    }
}
