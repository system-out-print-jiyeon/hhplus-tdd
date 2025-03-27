package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.lock.UserLockManager;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.hhplus.tdd.point.exception.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PointServiceTest {
    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);
    private UserPointRepository userPointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private UserLockManager lockManager;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointRepository = mock(UserPointRepository.class);
        pointHistoryRepository = mock(PointHistoryRepository.class);
        lockManager = new UserLockManager();
        pointService = new PointService(userPointRepository, pointHistoryRepository, lockManager);
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

    @Test
    void 포인트_정상_충전(){
        long userId = 1L;
        when(userPointRepository.findById(userId)).thenReturn(new UserPoint(userId, 100, System.currentTimeMillis()));
        when(userPointRepository.save(eq(userId), eq(300L)))
                .thenReturn(new UserPoint(userId, 300, System.currentTimeMillis()));
        UserPoint result = pointService.charge(userId, 200);

        assertEquals(300, result.point());
        verify(pointHistoryRepository).save(eq(userId), eq(200L), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 포인트_정상_사용(){
        long userId = 2L;
        when(userPointRepository.findById(userId)).thenReturn(new UserPoint(userId, 500, System.currentTimeMillis()));
        when(userPointRepository.save(eq(userId), eq(200L)))
                .thenReturn(new UserPoint(userId, 200, System.currentTimeMillis()));

        UserPoint result = pointService.use(userId, 300);

        assertEquals(200, result.point());
        verify(pointHistoryRepository).save(eq(userId), eq(300L), eq(TransactionType.USE), anyLong());
    }

    @Test
    void 포인트_사용_잔액부족() {
        long userId = 3L;
        when(userPointRepository.findById(userId)).thenReturn(new UserPoint(userId, 100, System.currentTimeMillis()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.use(userId, 200));
        assertEquals(INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void 음수_금액_충전시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.charge(1L, -10));
        assertEquals(INVALID_AMOUNT, exception.getMessage());
    }

    @Test
    void 음수_금액_사용시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.use(1L, 0));
        assertEquals(INVALID_AMOUNT, exception.getMessage());
    }

    @Test
    void ExecutorService_기반_동시_충전_테스트() throws InterruptedException {
        long userId = 42L;

        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        when(userPointRepository.save(eq(userId), anyLong()))
                .thenAnswer(invocation -> {
                    long savedAmount = invocation.getArgument(1);
                    log.info("[ExecutorService] 저장 요청: {} (by {})", savedAmount, Thread.currentThread().getName());
                    return new UserPoint(userId, savedAmount, System.currentTimeMillis());
                });

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int taskNumber = i;
            executor.submit(() -> {
                log.info("[ExecutorService] {}번 충전 시작 (by {})", taskNumber, Thread.currentThread().getName());
                pointService.charge(userId, 100);
                log.info("[ExecutorService] {}번 충전 완료 (by {})", taskNumber, Thread.currentThread().getName());
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        verify(userPointRepository, times(10)).save(eq(userId), anyLong());
        verify(pointHistoryRepository, times(10)).save(eq(userId), eq(100L), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void CompletableFuture_기반_동시_충전_테스트() {
        long userId = 42L;

        when(userPointRepository.findById(userId))
                .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        when(userPointRepository.save(eq(userId), anyLong()))
                .thenAnswer(invocation -> {
                    long savedAmount = invocation.getArgument(1);
                    log.info("[CompletableFuture] 저장 요청: {} (by {})", savedAmount, Thread.currentThread().getName());
                    return new UserPoint(userId, savedAmount, System.currentTimeMillis());
                });

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int taskNumber = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                log.info("[CompletableFuture] {}번 충전 시작 (by {})", taskNumber, Thread.currentThread().getName());
                pointService.charge(userId, 100);
                log.info("[CompletableFuture] {}번 충전 완료 (by {})", taskNumber, Thread.currentThread().getName());
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        verify(userPointRepository, times(10)).save(eq(userId), anyLong());
        verify(pointHistoryRepository, times(10)).save(eq(userId), eq(100L), eq(TransactionType.CHARGE), anyLong());
    }
}