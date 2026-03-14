package com.example.contract.service.impl;

import com.example.contract.dto.BatchPenaltyCalcRequest;
import com.example.contract.dto.PenaltyCalcRequest;
import com.example.contract.entity.Contract;
import com.example.contract.entity.RepaymentSchedule;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.RepaymentScheduleRepository;
import com.example.contract.service.PenaltyInterestService;
import com.example.contract.util.PenaltyInterestCalculator;
import com.example.contract.vo.ContractPenaltyVO;
import com.example.contract.vo.PenaltyCalcResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 罚息计算服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyInterestServiceImpl implements PenaltyInterestService {

    private final RepaymentScheduleRepository scheduleRepository;
    private final ContractRepository contractRepository;

    /**
     * 默认罚息年利率 24%
     */
    private static final BigDecimal DEFAULT_PENALTY_RATE = new BigDecimal("0.24");

    @Override
    public PenaltyCalcResultVO calculatePenalty(PenaltyCalcRequest request) {
        LocalDate calcDate = request.getCalcDate() != null ? request.getCalcDate() : LocalDate.now();
        BigDecimal penaltyRate = request.getPenaltyRate() != null ? request.getPenaltyRate() : DEFAULT_PENALTY_RATE;

        // 计算罚息
        PenaltyInterestCalculator.PenaltyCalcResult calcResult = PenaltyInterestCalculator.calculatePenalty(
                request.getRemainingRent(),
                penaltyRate,
                request.getPlanRepayDate(),
                calcDate,
                null
        );

        // 计算宽限期剩余天数
        int gracePeriodRemainingDays = calculateGracePeriodRemainingDays(request.getPlanRepayDate(), calcDate);

        return PenaltyCalcResultVO.builder()
                .scheduleId(request.getScheduleId())
                .remainingRent(request.getRemainingRent())
                .planRepayDate(request.getPlanRepayDate())
                .penaltyRate(penaltyRate)
                .dailyPenalty(calcResult.getDailyPenalty())
                .newPenalty(calcResult.getNewPenalty())
                .totalPenalty(calcResult.getTotalPenalty())
                .totalOverdueDays(calcResult.getTotalOverdueDays())
                .actualOverdueDays(calcResult.getActualOverdueDays())
                .rawOverdueDays(calcResult.getRawOverdueDays())
                .newOverdueDays(calcResult.getNewOverdueDays())
                .calcDate(calcDate)
                .isOverdue(calcResult.getActualOverdueDays() > 0)
                .inGracePeriod(calcResult.isInGracePeriod())
                .gracePeriodRemainingDays(gracePeriodRemainingDays)
                .build();
    }

    /**
     * 计算宽限期剩余天数
     *
     * @param planRepayDate 计划还款日期
     * @param calcDate      计算日期
     * @return 宽限期剩余天数，已过宽限期返回0
     */
    private int calculateGracePeriodRemainingDays(LocalDate planRepayDate, LocalDate calcDate) {
        int rawOverdueDays = PenaltyInterestCalculator.calculateRawOverdueDays(planRepayDate, calcDate);
        if (rawOverdueDays <= 0) {
            return PenaltyInterestCalculator.GRACE_PERIOD_DAYS;
        }
        int remaining = PenaltyInterestCalculator.GRACE_PERIOD_DAYS - rawOverdueDays;
        return Math.max(remaining, 0);
    }

    @Override
    public PenaltyCalcResultVO calculatePenaltyByScheduleId(Long scheduleId, LocalDate calcDate, BigDecimal penaltyRate) {
        // 查询还款计划
        RepaymentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("还款计划不存在: " + scheduleId));

        if (calcDate == null) {
            calcDate = LocalDate.now();
        }
        if (penaltyRate == null) {
            penaltyRate = DEFAULT_PENALTY_RATE;
        }

        return calculatePenaltyForSchedule(schedule, calcDate, penaltyRate);
    }

    @Override
    public ContractPenaltyVO calculateContractPenalty(BatchPenaltyCalcRequest request) {
        Long contractId = request.getContractId();
        LocalDate calcDate = request.getCalcDate() != null ? request.getCalcDate() : LocalDate.now();
        BigDecimal penaltyRate = request.getPenaltyRate() != null ? request.getPenaltyRate() : DEFAULT_PENALTY_RATE;

        // 查询合同信息
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("合同不存在: " + contractId));

        // 查询需要计算罚息的还款计划
        List<RepaymentSchedule> schedules = scheduleRepository.findSchedulesNeedPenaltyCalc(contractId, calcDate);

        // 计算各期罚息
        List<PenaltyCalcResultVO> penaltyDetails = new ArrayList<>();
        BigDecimal totalContractPenalty = BigDecimal.ZERO;
        int overduePeriodCount = 0;

        for (RepaymentSchedule schedule : schedules) {
            PenaltyCalcResultVO result = calculatePenaltyForSchedule(schedule, calcDate, penaltyRate);
            penaltyDetails.add(result);

            if (result.getIsOverdue()) {
                totalContractPenalty = totalContractPenalty.add(result.getTotalPenalty());
                overduePeriodCount++;
            }
        }

        return ContractPenaltyVO.builder()
                .contractId(contractId)
                .contractNo(contract.getContractNo())
                .lesseeName(contract.getLesseeName())
                .penaltyDetails(penaltyDetails)
                .totalContractPenalty(totalContractPenalty)
                .overduePeriodCount(overduePeriodCount)
                .calcDate(calcDate)
                .build();
    }

    @Override
    @Transactional
    public PenaltyCalcResultVO calculateAndUpdatePenalty(Long scheduleId, LocalDate calcDate, BigDecimal penaltyRate) {
        // 查询还款计划
        RepaymentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("还款计划不存在: " + scheduleId));

        if (calcDate == null) {
            calcDate = LocalDate.now();
        }
        if (penaltyRate == null) {
            penaltyRate = DEFAULT_PENALTY_RATE;
        }

        // 计算罚息
        PenaltyCalcResultVO result = calculatePenaltyForSchedule(schedule, calcDate, penaltyRate);

        // 更新还款计划的罚息信息
        if (result.getIsOverdue()) {
            schedule.setTotalPenaltyAmount(result.getTotalPenalty());
            schedule.setPenaltyCalcDate(calcDate);
            schedule.setRepayStatus(3); // 逾期状态
            scheduleRepository.save(schedule);
            log.info("更新还款计划[{}]罚息，累计罚息：{}，逾期天数：{}",
                    scheduleId, result.getTotalPenalty(), result.getTotalOverdueDays());
        }

        return result;
    }

    @Override
    @Transactional
    public List<PenaltyCalcResultVO> batchCalculateAndUpdatePenalty(Long contractId, LocalDate calcDate, BigDecimal penaltyRate) {
        if (calcDate == null) {
            calcDate = LocalDate.now();
        }
        if (penaltyRate == null) {
            penaltyRate = DEFAULT_PENALTY_RATE;
        }

        // 查询需要计算罚息的还款计划
        List<RepaymentSchedule> schedules = scheduleRepository.findSchedulesNeedPenaltyCalc(contractId, calcDate);

        List<PenaltyCalcResultVO> results = new ArrayList<>();
        for (RepaymentSchedule schedule : schedules) {
            PenaltyCalcResultVO result = calculatePenaltyForSchedule(schedule, calcDate, penaltyRate);
            results.add(result);

            // 更新罚息信息
            if (result.getIsOverdue()) {
                schedule.setTotalPenaltyAmount(result.getTotalPenalty());
                schedule.setPenaltyCalcDate(calcDate);
                schedule.setRepayStatus(3); // 逾期状态
                scheduleRepository.save(schedule);
            }
        }

        log.info("合同[{}]批量计算罚息完成，共计算{}条记录", contractId, results.size());
        return results;
    }

    @Override
    public PenaltyCalcResultVO calculatePenaltyForSchedule(RepaymentSchedule schedule, LocalDate calcDate, BigDecimal penaltyRate) {
        if (penaltyRate == null) {
            penaltyRate = DEFAULT_PENALTY_RATE;
        }

        // 获取剩余租金，如果为空则使用当期租金
        BigDecimal remainingRent = schedule.getRemainingRent() != null
                ? schedule.getRemainingRent()
                : schedule.getCurrentRent();

        // 计算罚息
        PenaltyInterestCalculator.PenaltyCalcResult calcResult = PenaltyInterestCalculator.calculatePenalty(
                remainingRent,
                penaltyRate,
                schedule.getPlanRepayDate(),
                calcDate,
                schedule.getPenaltyCalcDate()
        );

        // 计算宽限期剩余天数
        int gracePeriodRemainingDays = calculateGracePeriodRemainingDays(schedule.getPlanRepayDate(), calcDate);

        return PenaltyCalcResultVO.builder()
                .scheduleId(schedule.getId())
                .contractId(schedule.getContractId())
                .periodNo(schedule.getPeriodNo())
                .planRepayDate(schedule.getPlanRepayDate())
                .remainingRent(remainingRent)
                .penaltyRate(penaltyRate)
                .dailyPenalty(calcResult.getDailyPenalty())
                .newPenalty(calcResult.getNewPenalty())
                .totalPenalty(calcResult.getTotalPenalty())
                .totalOverdueDays(calcResult.getTotalOverdueDays())
                .actualOverdueDays(calcResult.getActualOverdueDays())
                .rawOverdueDays(calcResult.getRawOverdueDays())
                .newOverdueDays(calcResult.getNewOverdueDays())
                .calcDate(calcDate)
                .isOverdue(calcResult.getActualOverdueDays() > 0)
                .inGracePeriod(calcResult.isInGracePeriod())
                .gracePeriodRemainingDays(gracePeriodRemainingDays)
                .build();
    }
}
