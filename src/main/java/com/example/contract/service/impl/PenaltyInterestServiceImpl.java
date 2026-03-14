package com.example.contract.service.impl;

import com.example.contract.entity.RepaymentSchedule;
import com.example.contract.repository.RepaymentScheduleRepository;
import com.example.contract.service.PenaltyInterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 罚息计算服务实现类
 * 
 * 罚息计算公式: 当期剩余租金 * 0.24 / 365 = 当天的罚息
 * 
 * 逾期规则：
 * 1. 还款日当天晚上12点没还，算逾期1天
 * 2. 逾期前3天为宽限期，罚息为0
 * 3. 逾期第4天开始计算罚息，罚息按全部逾期天数计算（包括前3天）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyInterestServiceImpl implements PenaltyInterestService {

    private final RepaymentScheduleRepository repaymentScheduleRepository;

    @Override
    public int calculateOverdueDays(LocalDate dueDate, LocalDate calculateDate) {
        if (dueDate == null || calculateDate == null) {
            return 0;
        }
        if (!calculateDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, calculateDate);
    }

    @Override
    public int calculateEffectiveOverdueDays(int actualOverdueDays) {
        if (actualOverdueDays <= GRACE_PERIOD_DAYS) {
            return 0;
        }
        return actualOverdueDays;
    }

    @Override
    public BigDecimal calculateDailyPenaltyInterest(BigDecimal remainingRent) {
        if (remainingRent == null || remainingRent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return remainingRent
                .multiply(PENALTY_RATE)
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculatePenaltyInterest(BigDecimal remainingRent, Integer overdueDays) {
        if (remainingRent == null || remainingRent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int actualDays = overdueDays != null ? overdueDays : 0;
        int effectiveDays = calculateEffectiveOverdueDays(actualDays);
        if (effectiveDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dailyPenalty = calculateDailyPenaltyInterest(remainingRent);
        return dailyPenalty.multiply(new BigDecimal(effectiveDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalPenaltyInterest(RepaymentSchedule schedule, LocalDate calculateDate) {
        if (schedule == null) {
            return BigDecimal.ZERO;
        }
        if (calculateDate == null) {
            calculateDate = LocalDate.now();
        }
        LocalDate dueDate = schedule.getDueDate();
        if (dueDate == null || !calculateDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }
        int actualOverdueDays = calculateOverdueDays(dueDate, calculateDate);
        int effectiveOverdueDays = calculateEffectiveOverdueDays(actualOverdueDays);
        if (effectiveOverdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remainingRent = schedule.getRemainingRent();
        return calculatePenaltyInterest(remainingRent, actualOverdueDays);
    }

    @Override
    @Transactional
    public RepaymentSchedule calculateAndUpdatePenaltyInterest(Long scheduleId, LocalDate calculateDate) {
        RepaymentSchedule schedule = repaymentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("还款计划不存在, ID: " + scheduleId));
        
        if (calculateDate == null) {
            calculateDate = LocalDate.now();
        }
        
        LocalDate dueDate = schedule.getDueDate();
        if (dueDate != null && calculateDate.isAfter(dueDate)) {
            int actualOverdueDays = calculateOverdueDays(dueDate, calculateDate);
            int effectiveOverdueDays = calculateEffectiveOverdueDays(actualOverdueDays);
            
            schedule.setOverdueDays(actualOverdueDays);
            
            if (effectiveOverdueDays > 0) {
                schedule.setStatus(RepaymentSchedule.RepaymentStatus.OVERDUE);
                BigDecimal penaltyInterest = calculatePenaltyInterest(schedule.getRemainingRent(), actualOverdueDays);
                schedule.setPenaltyInterest(penaltyInterest);
                log.info("计算罚息成功, 还款计划ID: {}, 实际逾期天数: {}, 有效逾期天数: {}, 罚息金额: {}", 
                        scheduleId, actualOverdueDays, effectiveOverdueDays, penaltyInterest);
            } else {
                schedule.setPenaltyInterest(BigDecimal.ZERO);
                log.info("宽限期内, 还款计划ID: {}, 实际逾期天数: {}, 罚息金额: 0", 
                        scheduleId, actualOverdueDays);
            }
        }
        
        return repaymentScheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public List<RepaymentSchedule> calculateAllOverduePenaltyInterest(String contractNo, LocalDate calculateDate) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByContractNo(contractNo);
        
        if (calculateDate == null) {
            calculateDate = LocalDate.now();
        }
        
        for (RepaymentSchedule schedule : schedules) {
            LocalDate dueDate = schedule.getDueDate();
            if (dueDate != null && calculateDate.isAfter(dueDate)) {
                int actualOverdueDays = calculateOverdueDays(dueDate, calculateDate);
                int effectiveOverdueDays = calculateEffectiveOverdueDays(actualOverdueDays);
                
                schedule.setOverdueDays(actualOverdueDays);
                
                if (effectiveOverdueDays > 0) {
                    schedule.setStatus(RepaymentSchedule.RepaymentStatus.OVERDUE);
                    BigDecimal penaltyInterest = calculatePenaltyInterest(schedule.getRemainingRent(), actualOverdueDays);
                    schedule.setPenaltyInterest(penaltyInterest);
                } else {
                    schedule.setPenaltyInterest(BigDecimal.ZERO);
                }
            }
        }
        
        List<RepaymentSchedule> savedSchedules = repaymentScheduleRepository.saveAll(schedules);
        log.info("批量计算罚息成功, 合同编号: {}, 更新记录数: {}", contractNo, savedSchedules.size());
        
        return savedSchedules;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateContractTotalPenaltyInterest(String contractNo, LocalDate calculateDate) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByContractNo(contractNo);
        
        if (calculateDate == null) {
            calculateDate = LocalDate.now();
        }
        
        BigDecimal totalPenaltyInterest = BigDecimal.ZERO;
        
        for (RepaymentSchedule schedule : schedules) {
            BigDecimal penalty = calculateTotalPenaltyInterest(schedule, calculateDate);
            totalPenaltyInterest = totalPenaltyInterest.add(penalty);
        }
        
        return totalPenaltyInterest.setScale(2, RoundingMode.HALF_UP);
    }
}
