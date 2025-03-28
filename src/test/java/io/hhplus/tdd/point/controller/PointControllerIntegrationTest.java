package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointControllerIntegrationTest.class);

    @Autowired MockMvc mockMvc;

    @MockBean UserPointRepository userPointRepository;
    @MockBean PointHistoryRepository pointHistoryRepository;

    long userId = 1000L;

    @BeforeEach
    void setUp() {
        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
    }

    @Test
    void 포인트_조회_API() throws Exception {
        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 500, System.currentTimeMillis()));

        mockMvc.perform(get("/point/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(500));

        log.info("포인트 조회 테스트 완료");
    }

    @Test
    void 포인트_충전_API() throws Exception {
        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        when(userPointRepository.save(userId, 300))
                .thenReturn(new UserPoint(userId, 300, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/" + userId + "/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(300));

        log.info("포인트 충전 테스트 완료");
    }

    @Test
    void 포인트_사용_API() throws Exception {
        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 500, System.currentTimeMillis()));
        when(userPointRepository.save(userId, 300))
                .thenReturn(new UserPoint(userId, 300, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/" + userId + "/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(300));

        log.info("포인트 사용 테스트 완료");
    }

    @Test
    void 포인트_내역_조회_API() throws Exception {
        log.info("충전 먼저 진행");
        when(pointHistoryRepository.findAllByUserId(userId))
                .thenReturn(List.of(
                        new io.hhplus.tdd.point.model.PointHistory(1, userId, 100, TransactionType.CHARGE, System.currentTimeMillis()),
                        new io.hhplus.tdd.point.model.PointHistory(2, userId, 200, TransactionType.CHARGE, System.currentTimeMillis())
                ));

        mockMvc.perform(get("/point/" + userId + "/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].type", is(TransactionType.CHARGE.name())));

        log.info("포인트 내역 조회 테스트 완료");
    }
}