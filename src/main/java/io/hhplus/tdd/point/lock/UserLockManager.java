package io.hhplus.tdd.point.lock;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

@Component
public class UserLockManager {

    /**
     * 락 객체의 메모리 점유문제 해결을 위해
     * ConcurrentHashMap + synchronized 방식에서 WeakHashMap + synchronizedMap으로 변경
     */
    private final Map<Long, Object> lockMap = Collections.synchronizedMap(new WeakHashMap<>());

    // userId별로 락 객체를 생성
    public Object getLock(Long userId) {
        return lockMap.computeIfAbsent(userId, id -> new Object());
    }
}
