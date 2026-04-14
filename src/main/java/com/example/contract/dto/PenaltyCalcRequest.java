package com.example.contract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 罚息计算请求DTO
 */
@Data
public class PenaltyCalcRequest {

    /**
     * 还款计划ID
     */
    @NotNull(message = "还款计划ID不能为空")
    private Long scheduleId;

    /**
     * 当期剩余租金
     */
    @NotNull(message = "当期剩余租金不能为空")
    private BigDecimal remainingRent;

    /**
     * 罚息年利率（如 0.24 表示 24%），不传则使用默认 24%
     */
    private BigDecimal penaltyRate;

    /**
     * 计划还款日期
     */
    @NotNull(message = "计划还款日期不能为空")
    private LocalDate planRepayDate;

    /**
     * 计算日期（不传则默认为当前日期）
     */
    private LocalDate calcDate;
}
