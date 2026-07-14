package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.QStore;
import com.delivery.domain.store.entity.Store;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QStore store = QStore.store;

    public StoreRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Store> searchStores(UUID categoryId, String name, Pageable pageable) {
        List<Store> content = queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull(),
                        categoryIdEq(categoryId),
                        nameContains(name)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull(),
                        categoryIdEq(categoryId),
                        nameContains(name)
                )
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression categoryIdEq(UUID categoryId) {
        return categoryId != null ? store.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return (name != null && !name.isBlank()) ? store.name.containsIgnoreCase(name) : null;
    }
}