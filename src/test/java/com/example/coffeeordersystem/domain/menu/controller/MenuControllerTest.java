package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.common.dto.PageResponse;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.example.coffeeordersystem.domain.menu.service.MenuRankingService;
import com.example.coffeeordersystem.domain.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean
    private MenuRankingService menuRankingService;

    @Test
    @DisplayName("메뉴 목록 조회 성공 - 기본 파라미터")
    void getMenus_success() throws Exception {
        // given
        PageResponse<MenusGetResponse> response = new PageResponse<>(
                List.of(
                        new MenusGetResponse(1L, "아메리카노", 2000),
                        new MenusGetResponse(2L, "카페라떼", 2900)
                ),
                0, 1, 2, 10, true
        );
        given(menuService.getMenus(0, 10, null)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공 - 카테고리 필터")
    void getMenus_withCategory_success() throws Exception {
        // given
        PageResponse<MenusGetResponse> response = new PageResponse<>(
                List.of(new MenusGetResponse(1L, "아메리카노", 2000)),
                0, 1, 1, 10, true
        );
        given(menuService.getMenus(0, 10, MenuCategory.COFFEE)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/menus")
                        .param("category", "COFFEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("메뉴 목록 조회 실패 - 잘못된 카테고리 파라미터")
    void getMenus_invalidCategory_badRequest() throws Exception {
        mockMvc.perform(get("/api/menus")
                        .param("category", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
