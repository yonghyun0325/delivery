package com.delivery.domain.user.service;

import com.delivery.domain.user.UserDeletedEvent;
import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
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

    /**
     * 회원 자신 정보 조회
     *
     * @param userId 회원 PK키
     * @return User 정보 응답 객체
     */
    @Transactional(readOnly = true)
    public UserResponse findUserInfo(Long userId) {
        User user =
                userRepository
                        .findWithRolesByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
        return UserDtoMapper.toUserResponse(user);
    }

    /**
     * 닉네임 수정
     *
     * @param userId 회원 PK키
     * @param request 변경할 닉네임
     * @return 변경 후 User 정보 응답 객체
     */
    public UserResponse updateNickName(Long userId, @Valid UpdateNickNameRequest request) {
        User updatedUser = findActiveUser(userId);
        String nickName = request.nickName();

        if (nickName != null && !nickName.equals(updatedUser.getNickName())) {
            if (userRepository.existsByNickName(request.nickName()))
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }
        updatedUser.updateNickName(nickName);

        return UserDtoMapper.toUserResponse(updatedUser);
    }

    /**
     * 전화번호 수정
     *
     * @param userId 회원 PK키
     * @param request 변경할 전화번호
     * @return 변경 후 User 정보 응답 객체
     */
    public UserResponse updatePhoneNumber(Long userId, @Valid UpdatePhoneNumberRequest request) {
        User updatedUser = findActiveUser(userId);
        String phoneNumber = request.phoneNumber();

        updatedUser.updatePhoneNumber(phoneNumber);

        return UserDtoMapper.toUserResponse(updatedUser);
    }

    /**
     * 회원 삭제 삭제 후 이벤트 발생
     *
     * @param userId 탈퇴 회원 PK키
     */
    public void deleteUser(Long userId) {
        User deletedUser = findActiveUser(userId);
        String username = deletedUser.getUsername();
        deletedUser.delete(deletedUser.getUsername());
        applicationEventPublisher.publishEvent(new UserDeletedEvent(deletedUser.getId(), username));
        // TODO : 로그아웃도 해야함,
        // NOTE : 사용하는 쪽은 @EventListener
    }

    /**
     * 아이디 중복 체크
     *
     * @param username 입력한 아이디
     * @return 결과값 true, false
     */
    @Transactional(readOnly = true)
    public UserValidationResponse isDuplicationUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            return new UserValidationResponse(true);
        }
        return new UserValidationResponse(false);
    }

    /**
     * 닉네임중복 체크
     *
     * @param nickName 입력한 닉네임
     * @return 결과값 true, false
     */
    @Transactional(readOnly = true)
    public UserValidationResponse isDuplicationNickname(String nickName) {
        if (userRepository.existsByNickName(nickName)) {
            return new UserValidationResponse(true);
        }
        return new UserValidationResponse(false);
    }

    /**
     * 활성화 회원 조회
     *
     * @param userId 회원 PK키
     * @return 조회한 회원 User 엔티티 객체 반환
     */
    public User findActiveUser(Long userId) {
        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_EXIST_USER));
    }
}
