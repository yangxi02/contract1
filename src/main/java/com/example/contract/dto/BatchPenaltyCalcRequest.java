package com.example.contract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量罚息计算请求DTO
 */
@Data
public class BatchPenaltyCalcRequest {

    /**
     * 合同ID
     */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /**
     * 计算日期（不传则默认为当前日期）
     */
    private LocalDate calcDate;

    /**
     * 罚息年利率（如 0.24 表示 24%），不传则使用默认 24%
     */
    private java.math.BigDecimal penaltyRate;
}
