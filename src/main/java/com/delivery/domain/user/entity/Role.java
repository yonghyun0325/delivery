package com.delivery.domain.user.entity;

import com.delivery.domain.auth.exception.AuthErrorCode;
import com.delivery.global.exception.BusinessException;
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
            throw new BusinessException(AuthErrorCode.INVALID_ROLE);
        }
    }

    public static class Authority {
        public static final String MASTER = "ROLE_MASTER";
        public static final String MANAGER = "ROLE_MANAGER";
        public static final String OWNER = "ROLE_OWNER";
        public static final String CUSTOMER = "ROLE_CUSTOMER";
    }
}
