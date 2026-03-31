package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.common.dto.PageResponse;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.dto.MenusGetResponse;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public PageResponse<MenusGetResponse> getMenus(int page, int size, MenuCategory category) {
        Page<MenusGetResponse> result = menuRepository.findMenusWithConditions(PageRequest.of(page, size), category);
        return PageResponse.register(result);
    }
}
