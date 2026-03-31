package com.example.coffeeordersystem.domain.menu.repository;

import com.example.coffeeordersystem.common.config.jpa.JpaAuditingConfig;
import com.example.coffeeordersystem.common.config.querydsl.QueryDslConfig;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class MenuQueryRepositoryImplTest {

    @Autowired
    private MenuRepository menuRepository;

    @BeforeEach
    void setUp() {
        menuRepository.save(Menu.register("아메리카노", 2000, MenuCategory.COFFEE));
        menuRepository.save(Menu.register("카페라떼", 2900, MenuCategory.COFFEE));
        menuRepository.save(Menu.register("녹차라떼", 3500, MenuCategory.LATTE));
    }

    @Test
    @DisplayName("카테고리 필터 없이 전체 메뉴 조회")
    void findMenusWithConditions_noFilter() {
        Page<MenusGetResponse> result = menuRepository.findMenusWithConditions(PageRequest.of(0, 10), null);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("카테고리 필터로 메뉴 조회")
    void findMenusWithConditions_withCategory() {
        Page<MenusGetResponse> result = menuRepository.findMenusWithConditions(PageRequest.of(0, 10), MenuCategory.COFFEE);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(MenusGetResponse::name)
                .containsExactlyInAnyOrder("아메리카노", "카페라떼");
    }

    @Test
    @DisplayName("페이징 적용 조회")
    void findMenusWithConditions_paging() {
        Page<MenusGetResponse> result = menuRepository.findMenusWithConditions(PageRequest.of(0, 2), null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isLast()).isFalse();
    }
}
