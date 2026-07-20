package com.delivery.domain.user.entity;

import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserStatus {
    ACTIVE,
    DELETED;

    @JsonCreator
    public static UserStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(value)) {
                return userStatus;
            }
        }

        throw new UserException(UserErrorCode.INVALID_USER_STATUS);
    }
}
