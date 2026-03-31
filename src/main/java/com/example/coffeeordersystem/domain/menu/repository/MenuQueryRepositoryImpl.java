package com.example.coffeeordersystem.domain.menu.repository;

import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.consts.MenuStatus;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.coffeeordersystem.domain.menu.entity.QMenu.menu;

@RequiredArgsConstructor
public class MenuQueryRepositoryImpl implements MenuQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenusGetResponse> findMenusWithConditions(Pageable pageable, MenuCategory category) {
        List<MenusGetResponse> result = queryFactory
                .select(Projections.constructor(MenusGetResponse.class,
                        menu.id,
                        menu.name,
                        menu.price))
                .from(menu)
                .where(
                        menu.status.ne(MenuStatus.DISCONTINUED),
                        categoryEq(category)
                )
                .orderBy(menu.status.asc(), menu.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(menu.count())
                .from(menu)
                .where(
                        menu.status.ne(MenuStatus.DISCONTINUED),
                        categoryEq(category)
                )
                .fetchOne();

        if (total == null) total = 0L;

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanExpression categoryEq(MenuCategory category) {
        return category != null ? menu.category.eq(category) : null;
    }
}
