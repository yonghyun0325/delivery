package com.delivery.domain.store.repository;

import com.delivery.domain.store.entity.QStore;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.enums.StoreSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QStore store = QStore.store;

    public StoreRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Store> searchStores(UUID categoryId, UUID regionId, String name, StoreSortType sortType, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder()
                .and(store.deletedAt.isNull())
                .and(categoryIdEq(categoryId))
                .and(regionIdEq(regionId))
                .and(nameContains(name));

        List<Store> content = queryFactory
                .selectFrom(store)
                .where(builder)
                .orderBy(toOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (content.size() < pageable.getPageSize() && pageable.getOffset() == 0) {
            return new PageImpl<>(content, pageable, content.size());
        }

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
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
