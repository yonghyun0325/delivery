package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.response.UserAdminResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminService {
    public UserAdminResponse findUserInfo(Long id) {
        throw new UnsupportedOperationException("개발 중 입니다.");
    }

    public List<UserAdminResponse> findAllUserInfo() {
        throw new UnsupportedOperationException("개발 중 입니다.");
    }

    public void updateUserRole() {
        throw new UnsupportedOperationException("개발 중 입니다.");
    }
}
