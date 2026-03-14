package com.example.contract.service;

import com.example.contract.entity.RepaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 罚息计算服务接口
 * 
 * 逾期规则：
 * 1. 还款日当天晚上12点没还，算逾期1天
 * 2. 逾期前3天为宽限期，罚息为0
 * 3. 逾期第4天开始计算罚息，罚息按全部逾期天数计算（包括前3天）
 */
public interface PenaltyInterestService {

    BigDecimal PENALTY_RATE = new BigDecimal("0.24");
    BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    int GRACE_PERIOD_DAYS = 3;

    int calculateOverdueDays(LocalDate dueDate, LocalDate calculateDate);

    int calculateEffectiveOverdueDays(int actualOverdueDays);

    BigDecimal calculateDailyPenaltyInterest(BigDecimal remainingRent);

    BigDecimal calculatePenaltyInterest(BigDecimal remainingRent, Integer overdueDays);

    BigDecimal calculateTotalPenaltyInterest(RepaymentSchedule schedule, LocalDate calculateDate);

    RepaymentSchedule calculateAndUpdatePenaltyInterest(Long scheduleId, LocalDate calculateDate);

    List<RepaymentSchedule> calculateAllOverduePenaltyInterest(String contractNo, LocalDate calculateDate);

    BigDecimal calculateContractTotalPenaltyInterest(String contractNo, LocalDate calculateDate);
}
