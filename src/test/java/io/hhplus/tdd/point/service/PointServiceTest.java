package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointServiceTest {
    private UserPointRepository userPointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointRepository = mock(UserPointRepository.class);
        pointHistoryRepository = mock(PointHistoryRepository.class);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
    }

    @Test
    void 사용자_포인트_정상_조회(){
        long userId = 1L;
        when(userPointRepository.findById(userId)).thenReturn(new UserPoint(userId, 300, System.currentTimeMillis()));
        UserPoint result = pointService.get(userId);
        assertEquals(300, result.point());
    }

    @Test
    void 사용자_포인트_내역_정상_조회(){
        long userId = 2L;
        when(pointHistoryRepository.findAllByUserId(userId)).thenReturn(
                List.of(new PointHistory(1, userId, 200, TransactionType.USE, System.currentTimeMillis()))
        );
        List<PointHistory> histories = pointService.getHistories(userId);
        assertEquals(1, histories.size());

    }


}
