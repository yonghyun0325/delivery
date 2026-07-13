package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    /**
     * 권한으로 검색
     *
     * @param role
     * @return
     */
    public static Specification<User> equalRole(Role role) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role);
    }

    /**
     * 아이디로 검색
     *
     * @param username
     * @return
     */
    public static Specification<User> likeUsername(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("username"), "%" + username + "%");
    }

    /**
     * 날짜 검색
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<User> rangeDate(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            } else if (startDate == null && endDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            return null;
        };
    }

    /**
     * 회원 상태 필터링
     *
     * @return
     */
    public static Specification<User> userStatus(UserStatus userStatus) {
        return (root, query, criteriaBuilder) ->
                (userStatus == null)
                        ? null
                        : criteriaBuilder.equal(root.get("userStatus"), userStatus);
    }
}
