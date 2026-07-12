package com.delivery.domain.user.response;

import com.delivery.domain.user.enums.Role;
import java.util.Set;

public record UserResponse(String username, String nickName, String phoneNumber, Set<Role> roles) {}
