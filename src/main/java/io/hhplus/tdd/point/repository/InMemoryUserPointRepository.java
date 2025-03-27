package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.model.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserPointRepository implements UserPointRepository {

    private final UserPointTable userPointTable;

    public InMemoryUserPointRepository(UserPointTable userPointTable){
        this.userPointTable = userPointTable;
    }

    @Override
    public UserPoint findById(long userId){
        return userPointTable.selectById(userId);
    }

    public UserPoint save(long userId, long amount){
        return userPointTable.insertOrUpdate(userId, amount);
    }
}
