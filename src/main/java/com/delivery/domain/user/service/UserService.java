package com.delivery.domain.user.service;

import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findActiveUser(Long userId) {
        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
    }
}
