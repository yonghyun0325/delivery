package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.QStore;
import com.delivery.domain.store.entity.Store;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.delivery.domain.store.enums.StoreSortType;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;
import java.util.UUID;

public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QStore store = QStore.store;

    public StoreRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Store> searchStores(UUID categoryId, UUID regionId, String name, StoreSortType sortType, Pageable pageable) {
        List<Store> content = queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull(),
                        categoryIdEq(categoryId),
                        regionIdEq(regionId),
                        nameContains(name)
                )
                .orderBy(toOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull(),
                        categoryIdEq(categoryId),
                        regionIdEq(regionId),
                        nameContains(name)
                )
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression regionIdEq(UUID regionId) {
        return regionId != null ? store.regionId.eq(regionId) : null;
    }

    private BooleanExpression categoryIdEq(UUID categoryId) {
        return categoryId != null ? store.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return (name != null && !name.isBlank()) ? store.name.containsIgnoreCase(name) : null;
    }

    private OrderSpecifier<?> toOrderSpecifier(StoreSortType sortType) {
        if (sortType == StoreSortType.RATING_HIGH) {
            return store.averageRating.desc();
        }
        return store.createdAt.desc();
    }
}