package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;

    /**
     * 회원 단건 조회
     *
     * @param userId
     * @return User 상세 정보 응답 객체
     */
    public UserAdminResponse findUserInfo(Long userId) {
        User user =
                userRepository
                        .findWithRolesById(userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
        return UserDtoMapper.toUserAdminResponse(user);
    }

    /**
     * 회원 목록 조회
     *
     * @return User 정보 응답 객체
     */
    public List<UserAdminListResponse> findAllUserInfo() {
        return userRepository.findAllBy().stream()
                .map(UserDtoMapper::toUserAdminListResponse)
                .toList();
    }

    /**
     * 회원 권한 수정
     *
     * @param userId 회원 PK키
     * @param request 변경할 권한 정보
     */
    public void updateUserRole(long userId, UpdateUserRoleRequest request) {
        User user =
                userRepository
                        .findWithRolesById(userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
        user.updateRoles(request.role());
    }
}
