package com.delivery.domain.user.repository;

import com.delivery.common.util.QuerydslUtils;
import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.entity.QUser;
import com.delivery.domain.user.entity.User;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAQueryFactory queryFactory;
    QUser user = QUser.user;

    // Query
    @Override
    public Page<User> findAll(UserSearchRequest request, Pageable pageable) {
        OrderSpecifier<?>[] order = QuerydslUtils.getSort(pageable, QUser.user);

        List<User> result =
                queryFactory
                        .selectFrom(user)
                        .where(
                                QuerydslUtils.eq(user.userStatus, request.userStatus()),
                                QuerydslUtils.startsWith(user.username, request.username()),
                                QuerydslUtils.contains(user.roles, request.role()),
                                QuerydslUtils.createdAtBetween(user.createdAt, request.startDate(), request.endDate()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(order)
                        .fetch();

        if (result.size() < pageable.getPageSize() && pageable.getOffset() == 0) {
            return new PageImpl<>(result, pageable, result.size());
        }

        Long total =
                queryFactory
                        .select(user.count())
                        .from(user)
                        .where(
                                QuerydslUtils.eq(user.userStatus, request.userStatus()),
                                QuerydslUtils.startsWith(user.username, request.username()),
                                QuerydslUtils.contains(user.roles, request.role()),
                                QuerydslUtils.createdAtBetween(user.createdAt, request.startDate(), request.endDate()))
                        .fetchOne();

        return new PageImpl<>(result, pageable, total == null ? 0 : total);
    }
}
