package com.example.contract.service;

import com.example.contract.dto.BatchPenaltyCalcRequest;
import com.example.contract.dto.PenaltyCalcRequest;
import com.example.contract.entity.RepaymentSchedule;
import com.example.contract.vo.ContractPenaltyVO;
import com.example.contract.vo.PenaltyCalcResultVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 罚息计算服务接口
 */
public interface PenaltyInterestService {

    /**
     * 计算单期罚息
     *
     * @param request 罚息计算请求
     * @return 罚息计算结果
     */
    PenaltyCalcResultVO calculatePenalty(PenaltyCalcRequest request);

    /**
     * 计算单期罚息（根据还款计划ID）
     *
     * @param scheduleId 还款计划ID
     * @param calcDate   计算日期
     * @param penaltyRate 罚息年利率
     * @return 罚息计算结果
     */
    PenaltyCalcResultVO calculatePenaltyByScheduleId(Long scheduleId, LocalDate calcDate, BigDecimal penaltyRate);

    /**
     * 批量计算合同下所有逾期期数的罚息
     *
     * @param request 批量计算请求
     * @return 合同罚息汇总信息
     */
    ContractPenaltyVO calculateContractPenalty(BatchPenaltyCalcRequest request);

    /**
     * 计算并更新还款计划的罚息
     *
     * @param scheduleId  还款计划ID
     * @param calcDate    计算日期
     * @param penaltyRate 罚息年利率
     * @return 更新后的罚息金额
     */
    PenaltyCalcResultVO calculateAndUpdatePenalty(Long scheduleId, LocalDate calcDate, BigDecimal penaltyRate);

    /**
     * 批量计算并更新合同下所有逾期期数的罚息
     *
     * @param contractId  合同ID
     * @param calcDate    计算日期
     * @param penaltyRate 罚息年利率
     * @return 各期罚息计算结果列表
     */
    List<PenaltyCalcResultVO> batchCalculateAndUpdatePenalty(Long contractId, LocalDate calcDate, BigDecimal penaltyRate);

    /**
     * 根据还款计划计算罚息（不保存）
     *
     * @param schedule    还款计划
     * @param calcDate    计算日期
     * @param penaltyRate 罚息年利率
     * @return 罚息计算结果
     */
    PenaltyCalcResultVO calculatePenaltyForSchedule(RepaymentSchedule schedule, LocalDate calcDate, BigDecimal penaltyRate);
}
