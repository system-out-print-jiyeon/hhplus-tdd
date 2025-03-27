package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(long userId, long amount, TransactionType type, long timestamp);
    List<PointHistory> findAllByUserId(long userId);
}
