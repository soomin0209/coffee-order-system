package com.example.coffeeordersystem.domain.payment.controller;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.OrderExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.PaymentExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.PointExceptionEnum;
import com.example.coffeeordersystem.domain.payment.consts.PaymentStatus;
import com.example.coffeeordersystem.domain.payment.dto.PaymentResponse;
import com.example.coffeeordersystem.domain.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 성공")
    void executePayment_success() throws Exception {
        // given
        PaymentResponse response = new PaymentResponse(1L, "PAY-20260406-00000001", 5000, PaymentStatus.COMPLETED);

        given(paymentService.executePayment(1L)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/payments/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("결제 실행 성공"))
                .andExpect(jsonPath("$.data.paymentNumber").value("PAY-20260406-00000001"))
                .andExpect(jsonPath("$.data.amount").value(5000))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("결제 실패 - 존재하지 않는 주문")
    void executePayment_failed_orderNotFound() throws Exception {
        // given
        given(paymentService.executePayment(999L))
                .willThrow(new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다"));
    }

    @Test
    @DisplayName("결제 실패 - 결제 가능한 상태가 아닌 주문")
    void executePayment_failed_orderNotPayable() throws Exception {
        // given
        given(paymentService.executePayment(1L))
                .willThrow(new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_PAYABLE));

        // when & then
        mockMvc.perform(post("/api/payments/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 가능한 주문이 아닙니다"));
    }

    @Test
    @DisplayName("결제 실패 - 이미 결제 완료된 주문")
    void executePayment_failed_alreadyCompleted() throws Exception {
        // given
        given(paymentService.executePayment(1L))
                .willThrow(new ServiceErrorException(PaymentExceptionEnum.ERR_PAYMENT_ALREADY_COMPLETED));

        // when & then
        mockMvc.perform(post("/api/payments/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 결제 완료된 주문입니다"));
    }

    @Test
    @DisplayName("결제 실패 - 포인트 부족")
    void executePayment_failed_insufficientPoints() throws Exception {
        // given
        given(paymentService.executePayment(1L))
                .willThrow(new ServiceErrorException(PointExceptionEnum.ERR_POINT_INSUFFICIENT));

        // when & then
        mockMvc.perform(post("/api/payments/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("포인트가 부족합니다"));
    }
}