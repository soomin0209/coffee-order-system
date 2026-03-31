package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.common.dto.PageResponse;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Test
    @DisplayName("메뉴 목록 조회 성공")
    void getMenus_success() {
        // given
        List<MenusGetResponse> menus = List.of(
                new MenusGetResponse(1L, "아메리카노", 2000),
                new MenusGetResponse(2L, "카페라떼", 2900)
        );
        given(menuRepository.findMenusWithConditions(PageRequest.of(0, 10), null))
                .willReturn(new PageImpl<>(menus, PageRequest.of(0, 10), 2));

        // when
        PageResponse<MenusGetResponse> result = menuService.getMenus(0, 10, null);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공 - 카테고리 필터")
    void getMenus_withCategory_success() {
        // given
        List<MenusGetResponse> menus = List.of(
                new MenusGetResponse(1L, "아메리카노", 2000)
        );
        given(menuRepository.findMenusWithConditions(PageRequest.of(0, 10), MenuCategory.COFFEE))
                .willReturn(new PageImpl<>(menus, PageRequest.of(0, 10), 1));

        // when
        PageResponse<MenusGetResponse> result = menuService.getMenus(0, 10, MenuCategory.COFFEE);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);

        verify(menuRepository).findMenusWithConditions(PageRequest.of(0, 10), MenuCategory.COFFEE);
    }
}