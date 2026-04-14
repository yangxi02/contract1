package com.example.contract.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 罚息计算结果VO
 */
@Data
@Builder
public class PenaltyCalcResultVO {

    /**
     * 还款计划ID
     */
    private Long scheduleId;

    /**
     * 合同ID
     */
    private Long contractId;

    /**
     * 期数
     */
    private Integer periodNo;

    /**
     * 计划还款日期
     */
    private LocalDate planRepayDate;

    /**
     * 当期剩余租金
     */
    private BigDecimal remainingRent;

    /**
     * 罚息年利率
     */
    private BigDecimal penaltyRate;

    /**
     * 当天罚息金额
     */
    private BigDecimal dailyPenalty;

    /**
     * 新增罚息金额
     */
    private BigDecimal newPenalty;

    /**
     * 累计罚息金额
     */
    private BigDecimal totalPenalty;

    /**
     * 展示用的逾期天数（宽限期内显示为0）
     */
    private int totalOverdueDays;

    /**
     * 实际逾期天数（用于计算罚息，包含宽限期）
     */
    private int actualOverdueDays;

    /**
     * 原始逾期天数（包含宽限期3天，用于展示）
     */
    private int rawOverdueDays;

    /**
     * 新增逾期天数
     */
    private int newOverdueDays;

    /**
     * 计算日期
     */
    private LocalDate calcDate;

    /**
     * 是否逾期（已过宽限期）
     */
    private Boolean isOverdue;

    /**
     * 是否在宽限期内（逾期前3天）
     */
    private Boolean inGracePeriod;

    /**
     * 宽限期剩余天数
     */
    private Integer gracePeriodRemainingDays;
}
