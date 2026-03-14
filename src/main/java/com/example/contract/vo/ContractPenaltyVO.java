package com.example.contract.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同罚息汇总VO
 */
@Data
@Builder
public class ContractPenaltyVO {

    /**
     * 合同ID
     */
    private Long contractId;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 承租人名称
     */
    private String lesseeName;

    /**
     * 各期罚息明细
     */
    private List<PenaltyCalcResultVO> penaltyDetails;

    /**
     * 合同总罚息金额
     */
    private BigDecimal totalContractPenalty;

    /**
     * 逾期期数
     */
    private Integer overduePeriodCount;

    /**
     * 计算日期
     */
    private java.time.LocalDate calcDate;
}
