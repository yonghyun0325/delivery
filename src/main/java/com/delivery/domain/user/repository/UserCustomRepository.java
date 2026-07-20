package com.delivery.domain.user.repository;

import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomRepository {
    Page<User> findAll(UserSearchRequest request, Pageable pageable);
}
