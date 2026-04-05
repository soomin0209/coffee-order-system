package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.common.dto.BaseResponse;
import com.example.coffeeordersystem.common.dto.PageResponse;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.dto.MenuRankingResponse;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.example.coffeeordersystem.domain.menu.service.MenuRankingService;
import com.example.coffeeordersystem.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private final MenuRankingService menuRankingService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<MenusGetResponse>>> getMenus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) MenuCategory category
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "메뉴 목록 조회 성공", menuService.getMenus(page, size, category)));
    }

    @GetMapping("/ranking")
    public ResponseEntity<BaseResponse<List<MenuRankingResponse>>> findMenuRankingTop3InLast7Days() {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "인기 메뉴 목록 조회 성공", menuRankingService.findMenuRankingTop3InLast7Days()));
    }
}
