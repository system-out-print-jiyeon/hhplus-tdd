package io.hhplus.tdd.point.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserPointTest {

    @Test
    void 빈_포인트_생성_테스트() {
        long userId = 1L;
        UserPoint empty = UserPoint.empty(userId);
        assertEquals(userId, empty.id());
        assertEquals(0, empty.point());
        assertTrue(empty.updateMillis() > 0);
    }

    @Test
    void 포인트_정상_저장(){
        UserPoint up = new UserPoint(1L, 100L, 123456789L);
        assertEquals(1L, up.id());
        assertEquals(100L, up.point());
        assertEquals(123456789L, up.updateMillis());
    }
}
