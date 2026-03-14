package com.example.contract.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "repayment_schedule")
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "合同ID不能为空")
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @NotBlank(message = "合同编号不能为空")
    @Column(name = "contract_no", nullable = false, length = 50)
    private String contractNo;

    @NotNull(message = "期数不能为空")
    @Min(value = 1, message = "期数至少为1")
    @Column(name = "period", nullable = false)
    private Integer period;

    @NotNull(message = "应还日期不能为空")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "应还本金不能为空")
    @DecimalMin(value = "0.00", message = "应还本金不能小于0")
    @Column(name = "principal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @NotNull(message = "应还利息不能为空")
    @DecimalMin(value = "0.00", message = "应还利息不能小于0")
    @Column(name = "interest_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @NotNull(message = "应还租金不能为空")
    @DecimalMin(value = "0.00", message = "应还租金不能小于0")
    @Column(name = "rent_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentAmount;

    @DecimalMin(value = "0.00", message = "已还本金不能小于0")
    @Column(name = "paid_principal", precision = 18, scale = 2)
    private BigDecimal paidPrincipal;

    @DecimalMin(value = "0.00", message = "已还利息不能小于0")
    @Column(name = "paid_interest", precision = 18, scale = 2)
    private BigDecimal paidInterest;

    @DecimalMin(value = "0.00", message = "已还租金不能小于0")
    @Column(name = "paid_rent", precision = 18, scale = 2)
    private BigDecimal paidRent;

    @DecimalMin(value = "0.00", message = "剩余本金不能小于0")
    @Column(name = "remaining_principal", precision = 18, scale = 2)
    private BigDecimal remainingPrincipal;

    @DecimalMin(value = "0.00", message = "剩余租金不能小于0")
    @Column(name = "remaining_rent", precision = 18, scale = 2)
    private BigDecimal remainingRent;

    @Column(name = "actual_payment_date")
    private LocalDate actualPaymentDate;

    @Column(name = "overdue_days")
    private Integer overdueDays;

    @DecimalMin(value = "0.00", message = "罚息金额不能小于0")
    @Column(name = "penalty_interest", precision = 18, scale = 2)
    private BigDecimal penaltyInterest;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RepaymentStatus {
        PENDING,        
        PAID,           
        PARTIAL_PAID,   
        OVERDUE,        
        WRITTEN_OFF     
    }

    public BigDecimal getPaidPrincipal() {
        return paidPrincipal != null ? paidPrincipal : BigDecimal.ZERO;
    }

    public BigDecimal getPaidInterest() {
        return paidInterest != null ? paidInterest : BigDecimal.ZERO;
    }

    public BigDecimal getPaidRent() {
        return paidRent != null ? paidRent : BigDecimal.ZERO;
    }

    public BigDecimal getRemainingPrincipal() {
        return remainingPrincipal != null ? remainingPrincipal : principalAmount;
    }

    public BigDecimal getRemainingRent() {
        return remainingRent != null ? remainingRent : rentAmount;
    }

    public Integer getOverdueDays() {
        return overdueDays != null ? overdueDays : 0;
    }

    public BigDecimal getPenaltyInterest() {
        return penaltyInterest != null ? penaltyInterest : BigDecimal.ZERO;
    }
}
