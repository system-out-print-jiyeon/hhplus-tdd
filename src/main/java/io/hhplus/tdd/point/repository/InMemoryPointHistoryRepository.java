package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;

import java.util.List;

public class InMemoryPointHistoryRepository implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public InMemoryPointHistoryRepository(PointHistoryTable pointHistoryTable){
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public PointHistory save(long userId, long amount, TransactionType type, long timestamp){
        return pointHistoryTable.insert(userId, amount, type, timestamp);
    }

    public List<PointHistory> findAllByUserId(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

}
