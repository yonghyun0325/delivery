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
import com.delivery.domain.user.repository.UserSpecification;
import java.util.Set;

import com.delivery.global.cache.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        Specification<User> spec = createUserSpecification(request);
        pageable = validatedPageable(pageable);

        Page<User> userPage = userRepository.findAll(spec, pageable);
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

    private Pageable validatedPageable(Pageable pageable) {
        int size = (pageable != null) ? pageable.getPageSize() : 10;
        int validatedSize = Set.of(10, 20, 30).contains(size) ? size : 10;

        return PageRequest.of(
                (pageable != null) ? pageable.getPageNumber() : 0,
                validatedSize,
                (pageable != null) ? pageable.getSort() : Sort.unsorted());
    }

    private Specification<User> createUserSpecification(UserSearchRequest request) {
        Specification<User> spec = Specification.allOf();

        if (request.userStatus() != null) {
            spec = spec.and(UserSpecification.userStatus(request.userStatus()));
        }
        if (request.username() != null && !request.username().isBlank()) {
            spec = spec.and(UserSpecification.likeUsername(request.username()));
        }
        if (request.role() != null) {
            spec = spec.and(UserSpecification.equalRole(request.role()));
        }
        if (request.startDate() != null || request.endDate() != null) {
            spec = spec.and(UserSpecification.rangeDate(request.startDate(), request.endDate()));
        }

        return spec;
    }
}
