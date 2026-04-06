package com.example.coffeeordersystem.domain.order.controller;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.MenuExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.UserExceptionEnum;
import com.example.coffeeordersystem.domain.order.consts.OrderStatus;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateResponse;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 2);
        OrderCreateResponse response = new OrderCreateResponse(1L, "ORD-20260406-00000001", 8000, OrderStatus.PENDING);

        given(orderService.createOrder(any(OrderCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문 생성 성공"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-20260406-00000001"))
                .andExpect(jsonPath("$.data.totalPrice").value(8000))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("주문 생성 실패 - userId 누락")
    void createOrder_failed_missingUserId() throws Exception {
        // given - userId null
        OrderCreateRequest request = new OrderCreateRequest(null, 1L, 2);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 아이디는 필수입니다"));
    }

    @Test
    @DisplayName("주문 생성 실패 - menuId 누락")
    void createOrder_failed_missingMenuId() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, null, 2);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("메뉴 아이디는 필수입니다"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 수량 0")
    void createOrder_failed_zeroQuantity() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 0);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("1개 이상부터 주문 가능합니다"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 유저")
    void createOrder_failed_userNotFound() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(999L, 1L, 1);

        given(orderService.createOrder(any(OrderCreateRequest.class)))
                .willThrow(new ServiceErrorException(UserExceptionEnum.ERR_USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 판매 중이 아닌 메뉴")
    void createOrder_failed_menuNotOnSale() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 1);

        given(orderService.createOrder(any(OrderCreateRequest.class)))
                .willThrow(new ServiceErrorException(MenuExceptionEnum.ERR_MENU_NOT_ON_SALE));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("판매 중이지 않은 메뉴입니다"));
    }
}