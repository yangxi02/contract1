package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 还款计划表实体类
 * 用于记录融资租赁合同的每期还款计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "repayment_schedules")
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 合同ID
     */
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    /**
     * 期数
     */
    @Column(name = "period_no", nullable = false)
    private Integer periodNo;

    /**
     * 计划还款日期
     */
    @Column(name = "plan_repay_date", nullable = false)
    private LocalDate planRepayDate;

    /**
     * 实际还款日期
     */
    @Column(name = "actual_repay_date")
    private LocalDate actualRepayDate;

    /**
     * 当期租金
     */
    @Column(name = "current_rent", nullable = false, precision = 18, scale = 2)
    private BigDecimal currentRent;

    /**
     * 当期本金
     */
    @Column(name = "current_principal", nullable = false, precision = 18, scale = 2)
    private BigDecimal currentPrincipal;

    /**
     * 当期利息
     */
    @Column(name = "current_interest", nullable = false, precision = 18, scale = 2)
    private BigDecimal currentInterest;

    /**
     * 当期剩余租金（未还金额）
     */
    @Column(name = "remaining_rent", precision = 18, scale = 2)
    private BigDecimal remainingRent;

    /**
     * 还款状态：0-未还, 1-已还, 2-部分还款, 3-逾期
     */
    @Column(name = "repay_status", nullable = false, length = 1)
    @Builder.Default
    private Integer repayStatus = 0;

    /**
     * 累计罚息金额
     */
    @Column(name = "total_penalty_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalPenaltyAmount = BigDecimal.ZERO;

    /**
     * 罚息计算截止日期（上次计算罚息的日期）
     */
    @Column(name = "penalty_calc_date")
    private LocalDate penaltyCalcDate;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否删除：0-未删除, 1-已删除
     */
    @Column(name = "is_deleted", length = 1)
    @Builder.Default
    private Integer isDeleted = 0;
}
