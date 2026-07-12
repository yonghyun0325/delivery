package com.delivery.domain.user.service;

import com.delivery.domain.user.UserDeletedEvent;
import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.UpdateUserRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.dto.response.UserValidationResponse;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponse findUserInfo(Long userId) {
        User user =
                userRepository
                        .findWithRolesByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
        return UserDtoMapper.toDto(user);
    }

    // 회원 정보 업데이트
    public UserResponse updateUser(Long userId, @Valid UpdateUserRequest request) {
        User updatedUser = findActiveUser(userId);
        String nickName = request.nickName();
        String phoneNumber = request.phoneNumber();

        if (nickName != null && !nickName.equals(updatedUser.getNickName())) {
            if(userRepository.existsByNickName(request.nickName())) throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        updatedUser.update(nickName, phoneNumber);

        return UserDtoMapper.toDto(updatedUser);
    }

    // 회원 정보 삭제
    public void deleteUser(Long userId) {
        User deletedUser = findActiveUser(userId);
        String username = deletedUser.getUsername();
        deletedUser.delete(deletedUser.getUsername());
        applicationEventPublisher.publishEvent(new UserDeletedEvent(deletedUser.getId(), username));
        // TODO : 로그아웃도 해야함,
        // NOTE : 사용하는 쪽은 @EventListener
    }

    @Transactional(readOnly = true)
    public UserValidationResponse isDuplicationUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            return new UserValidationResponse(true);
        }
        return new UserValidationResponse(false);
    }

    @Transactional(readOnly = true)
    public UserValidationResponse isDuplicationNickname(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            return new UserValidationResponse(true);
        }
        return new UserValidationResponse(false);
    }

    public User findActiveUser(Long userId) {
        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
    }
}
