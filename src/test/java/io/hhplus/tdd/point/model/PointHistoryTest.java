package io.hhplus.tdd.point.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointHistoryTest {

    @Test
    void 포인트_히스토리_정상_생성() {
        PointHistory history = new PointHistory(1L, 2L, 100L, TransactionType.USE, 123456789L);

        assertEquals(1L, history.id());
        assertEquals(2L, history.userId());
        assertEquals(100L, history.amount());
        assertEquals(TransactionType.USE, history.type());
        assertEquals(123456789L, history.updateMillis());
    }
}
