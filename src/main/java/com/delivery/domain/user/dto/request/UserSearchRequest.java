package com.delivery.domain.user.dto.request;

import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.UserStatus;
import java.time.LocalDateTime;

public record UserSearchRequest(
        Role role,
        UserStatus userStatus,
        String username,
        LocalDateTime startDate,
        LocalDateTime endDate) {}
