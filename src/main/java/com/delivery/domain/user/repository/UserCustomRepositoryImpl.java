package com.delivery.domain.user.repository;

import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.entity.QUser;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAQueryFactory queryFactory;
    QUser user = QUser.user;

    // Query
    @Override
    public Page<User> findAll(UserSearchRequest request, Pageable pageable) {
        List<User> result =
                queryFactory
                        .selectFrom(user)
                        .where(
                                userStatusEq(request.userStatus()),
                                usernameStartsWith(request.username()),
                                roleEq(request.role()),
                                createdAtBetween(request.startDate(), request.endDate()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(user.createdAt.desc())
                        .fetch();

        Long total =
                queryFactory
                        .select(user.count())
                        .from(user)
                        .where(
                                userStatusEq(request.userStatus()),
                                usernameStartsWith(request.username()),
                                roleEq(request.role()),
                                createdAtBetween(request.startDate(), request.endDate()))
                        .fetchOne();

        return new PageImpl<>(result, pageable, total == null ? 0 : total);
    }

    // BooleanExpression
    // 유저 상태
    private BooleanExpression userStatusEq(UserStatus userStatus) {
        return userStatus == null ? null : user.userStatus.eq(userStatus);
    }

    // 유저 아이디
    private BooleanExpression usernameStartsWith(String username) {
        return username == null || username.isBlank() ? null : user.username.startsWith(username);
    }

    // 유저 권한
    private BooleanExpression roleEq(Role role) {
        return role == null ? null : user.roles.contains(role);
    }

    // 검색 기간
    private BooleanExpression createdAtBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return user.createdAt.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        if (startDate != null) {
            return user.createdAt.goe(startDate.atStartOfDay());
        }

        if (endDate != null) {
            return user.createdAt.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }
}
