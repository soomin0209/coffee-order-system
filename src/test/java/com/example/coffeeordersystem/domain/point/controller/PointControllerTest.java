package com.example.coffeeordersystem.domain.point.controller;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.UserExceptionEnum;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeResponse;
import com.example.coffeeordersystem.domain.point.service.PointService;
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

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePoints_success() throws Exception {
        // given
        PointsChargeRequest request = new PointsChargeRequest(1L, 10000);
        PointsChargeResponse response = new PointsChargeResponse(1L, 1L, 10000, 10000);

        given(pointService.chargePoints(any(PointsChargeRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(10000))
                .andExpect(jsonPath("$.data.pointBalance").value(10000));
    }

    @Test
    @DisplayName("포인트 충전 실패 - amount 0 이하")
    void chargePoints_invalidAmount() throws Exception {
        // given
        PointsChargeRequest request = new PointsChargeRequest(1L, 0);

        // when & then
        mockMvc.perform(post("/api/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("포인트 충전 실패 - 존재하지 않는 유저")
    void chargePoints_notFoundUser() throws Exception {
        //given
        PointsChargeRequest request = new PointsChargeRequest(999L, 10000);

        given(pointService.chargePoints(any(PointsChargeRequest.class)))
                .willThrow(new ServiceErrorException(UserExceptionEnum.ERR_USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}