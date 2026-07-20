package com.delivery.domain.user.service;

import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.dto.response.PageResponse;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;

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
    public PageResponse<UserAdminListResponse> findAllUserInfo(
            UserSearchRequest request, Pageable pageable) {

        validateDateRange(request);
        pageable = validatedPageable(pageable);

        Page<User> userPage = userRepository.findAll(request, pageable);
        return PageResponse.of(userPage.map(UserDtoMapper::toUserAdminListResponse));
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
        userCacheRepository.delete(user.getUserUuid());
    }

    /**
     * 페이징 검색 사이즈 체크 및 변환
     *
     * @param pageable
     * @return
     */
    private Pageable validatedPageable(Pageable pageable) {
        int size = (pageable != null) ? pageable.getPageSize() : 10;
        int validatedSize = Set.of(10, 20, 30).contains(size) ? size : 10;

        return PageRequest.of(
                (pageable != null) ? pageable.getPageNumber() : 0,
                validatedSize,
                (pageable != null) ? pageable.getSort() : Sort.unsorted());
    }

    /**
     * 날짜 조건 쿼리
     *
     * @param request
     */
    private void validateDateRange(UserSearchRequest request) {
        if (request.startDate() != null
                && request.endDate() != null
                && request.startDate().isAfter(request.endDate())) {
            throw new BusinessException(GlobalErrorCode.INVALID_DATE_RANGE);
        }
    }
}
