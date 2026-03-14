package com.example.contract.repository;

import com.example.contract.entity.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 还款计划表Repository
 */
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    /**
     * 根据合同ID查询还款计划列表
     */
    List<RepaymentSchedule> findByContractIdAndIsDeletedOrderByPeriodNoAsc(Long contractId, Integer isDeleted);

    /**
     * 根据合同ID和还款状态查询
     */
    List<RepaymentSchedule> findByContractIdAndRepayStatusAndIsDeleted(Long contractId, Integer repayStatus, Integer isDeleted);

    /**
     * 查询逾期的还款计划（计划还款日期已过但未还清）
     */
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.contractId = :contractId " +
           "AND r.planRepayDate < :currentDate " +
           "AND r.repayStatus IN (0, 2) " +
           "AND r.isDeleted = 0 " +
           "ORDER BY r.periodNo ASC")
    List<RepaymentSchedule> findOverdueSchedules(@Param("contractId") Long contractId, @Param("currentDate") LocalDate currentDate);

    /**
     * 根据合同ID和期数查询
     */
    Optional<RepaymentSchedule> findByContractIdAndPeriodNoAndIsDeleted(Long contractId, Integer periodNo, Integer isDeleted);

    /**
     * 查询需要计算罚息的还款计划
     * 条件：未还清 或 部分还款，且计划还款日期已过期
     */
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.contractId = :contractId " +
           "AND r.planRepayDate < :calcDate " +
           "AND r.remainingRent > 0 " +
           "AND r.isDeleted = 0 " +
           "ORDER BY r.periodNo ASC")
    List<RepaymentSchedule> findSchedulesNeedPenaltyCalc(@Param("contractId") Long contractId, @Param("calcDate") LocalDate calcDate);
}
