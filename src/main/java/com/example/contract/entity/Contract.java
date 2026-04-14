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
 * 融资租赁合同实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 合同编号
     */
    @NotBlank(message = "合同编号不能为空")
    @Column(name = "contract_no", nullable = false, unique = true, length = 50)
    private String contractNo;

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空")
    @Column(name = "contract_name", nullable = false, length = 200)
    private String contractName;

    /**
     * 项目编号
     */
    @NotBlank(message = "项目编号不能为空")
    @Column(name = "project_no", nullable = false, length = 50)
    private String projectNo;

    /**
     * 承租人名称
     */
    @NotBlank(message = "承租人名称不能为空")
    @Column(name = "lessee_name", nullable = false, length = 200)
    private String lesseeName;

    /**
     * 出租人名称
     */
    @NotBlank(message = "出租人名称不能为空")
    @Column(name = "lessor_name", nullable = false, length = 200)
    private String lessorName;

    /**
     * 合同金额
     */
    @NotNull(message = "合同金额不能为空")
    @DecimalMin(value = "0.00", message = "合同金额不能小于0")
    @Column(name = "contract_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal contractAmount;

    /**
     * 租赁期限(月)
     */
    @NotNull(message = "租赁期限不能为空")
    @Min(value = 1, message = "租赁期限至少为1个月")
    @Column(name = "lease_term", nullable = false)
    private Integer leaseTerm;

    /**
     * 租赁利率(%)
     */
    @NotNull(message = "租赁利率不能为空")
    @DecimalMin(value = "0.00", message = "租赁利率不能小于0")
    @Column(name = "lease_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal leaseRate;

    /**
     * 合同开始日期
     */
    @NotNull(message = "合同开始日期不能为空")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 合同结束日期
     */
    @NotNull(message = "合同结束日期不能为空")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 还款方式
     */
    @NotBlank(message = "还款方式不能为空")
    @Column(name = "repayment_method", nullable = false, length = 50)
    private String repaymentMethod;

    /**
     * 保证金金额
     */
    @DecimalMin(value = "0.00", message = "保证金金额不能小于0")
    @Column(name = "deposit_amount", precision = 18, scale = 2)
    private BigDecimal depositAmount;

    /**
     * 服务费金额
     */
    @DecimalMin(value = "0.00", message = "服务费金额不能小于0")
    @Column(name = "service_fee", precision = 18, scale = 2)
    private BigDecimal serviceFee;

    /**
     * 合同状态
     */
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    /**
     * 合同签订日期
     */
    @Column(name = "sign_date")
    private LocalDate signDate;

    /**
     * 合同存放位置
     */
    @Column(name = "storage_location", length = 500)
    private String storageLocation;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

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

    @Column(name = "customer")
    private String  customerName ;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    /**
     * 合同状态枚举
     */
    public enum ContractStatus {
        DRAFT,          // 草稿
        PENDING,        // 待审批
        APPROVED,       // 已审批
        SIGNED,         // 已签订
        IN_PROGRESS,    // 执行中
        COMPLETED,      // 已完成
        TERMINATED,     // 已终止
        CANCELLED       // 已取消
    }
}
