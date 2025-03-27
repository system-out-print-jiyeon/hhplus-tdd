package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.exception.ErrorMessages.*;

@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(UserPointRepository userPointRepository,
                        PointHistoryRepository pointHistoryRepository){
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    public UserPoint get(long userId) {
        return userPointRepository.findById(userId);
    }

    public List<PointHistory> getHistories(long userId){
        return pointHistoryRepository.findAllByUserId(userId);
    }

    public UserPoint charge(long userId, long amount){
        validateAmount(amount);
        UserPoint current = userPointRepository.findById(userId);
        long newAmount = current.point() + amount;
        UserPoint updatedPoint = userPointRepository.save(userId, newAmount);
        pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, updatedPoint.updateMillis());
        return updatedPoint;
    }

    public UserPoint use(long userId, long amount){
        validateAmount(amount);
        UserPoint current = userPointRepository.findById(userId);
        if(current.point() < amount){
            throw new IllegalArgumentException(INSUFFICIENT_BALANCE);
        }
        long newAmount = current.point() - amount;
        UserPoint updatedPoint = userPointRepository.save(userId, newAmount);
        pointHistoryRepository.save(userId, amount, TransactionType.USE, updatedPoint.updateMillis());
        return updatedPoint;
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(INVALID_AMOUNT);
        }
    }


}
